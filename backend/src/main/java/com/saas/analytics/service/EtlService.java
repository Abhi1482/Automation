package com.saas.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.saas.analytics.model.*;
import com.saas.analytics.repository.*;
import com.saas.analytics.util.AESUtil;
import com.saas.analytics.util.UTMParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class EtlService {

    private final UserRepository userRepository;
    private final CostEngineService costEngineService;
    private final CostConfigRepository costConfigRepository;
    private final HourlyMetricRepository hourlyMetricRepository;
    private final ProductRepository productRepository;
    private final IntegrationService integrationService;
    private final AESUtil aesUtil;

    public EtlService(UserRepository userRepository,
                      CostEngineService costEngineService,
                      CostConfigRepository costConfigRepository,
                      HourlyMetricRepository hourlyMetricRepository,
                      ProductRepository productRepository,
                      IntegrationService integrationService,
                      AESUtil aesUtil) {
        this.userRepository = userRepository;
        this.costEngineService = costEngineService;
        this.costConfigRepository = costConfigRepository;
        this.hourlyMetricRepository = hourlyMetricRepository;
        this.productRepository = productRepository;
        this.integrationService = integrationService;
        this.aesUtil = aesUtil;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void runEtlJob() {
        log.info("Starting ETL Job");

        for (User user : userRepository.findAll()) {
            try {
                processUser(user);
            } catch (Exception e) {
                log.error("ETL failed for user {}", user.getId(), e);
            }
        }

        log.info("Finished ETL Job");
    }

    @Transactional
    public void processUser(User user) {

        CostConfig config = costConfigRepository.findByUserId(user.getId()).orElse(new CostConfig());

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDate date = nowUtc.toLocalDate();
        String dateStr = date.toString();

        List<IntegrationMeta> metaAccounts = integrationService.getUserMetaAccounts(user.getId());
        List<IntegrationShopify> shopifyStores = integrationService.getUserShopifyStores(user.getId());

        // ---------- PRODUCT COST MAP ----------
        Map<String, BigDecimal> productCostMap = new HashMap<>();
        for (Product p : productRepository.findAllByUserId(user.getId())) {
            productCostMap.put(p.getShopifyProductId(), p.getCost());
        }

        // ---------- META HOURLY SPEND ----------
        Map<Integer, BigDecimal> hourlyMetaSpend = new HashMap<>();
        for (int i = 0; i < 24; i++) hourlyMetaSpend.put(i, BigDecimal.ZERO);

        for (IntegrationMeta meta : metaAccounts) {
            Map<Integer, BigDecimal> spendMap = fetchMetaSpendHourly(meta, dateStr);

            for (int h = 0; h < 24; h++) {
                hourlyMetaSpend.put(
                        h,
                        hourlyMetaSpend.get(h).add(spendMap.getOrDefault(h, BigDecimal.ZERO))
                );
            }
        }

        log.info("FINAL META SPEND MAP: {}", hourlyMetaSpend);

        // ---------- GROUP DATA ----------
        class GroupData {
            BigDecimal revenue = BigDecimal.ZERO;
            int orders = 0;
            int quantity = 0;
            BigDecimal productCost = BigDecimal.ZERO;
        }

        Map<Integer, Map<String, GroupData>> hourMatrix = new HashMap<>();
        Map<Integer, BigDecimal> hourTotalRevenue = new HashMap<>();

        for (int h = 0; h < 24; h++) {
            hourMatrix.put(h, new HashMap<>());
            hourMatrix.get(h).put("ALL", new GroupData());
            hourTotalRevenue.put(h, BigDecimal.ZERO);
        }

        // ---------- FETCH SHOPIFY ----------
        for (IntegrationShopify shopify : shopifyStores) {

            String token = getShopifyAccessToken(shopify);
            if (token == null) continue;

            JsonNode orders = fetchShopifyOrders(shopify, token, nowUtc);
            if (orders == null || !orders.isArray()) continue;

            for (JsonNode order : orders) {

                try {
                    String createdAt = order.path("created_at").asText();
                    int hour = ZonedDateTime.parse(createdAt)
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .getHour();

                    String source = UTMParser.extractUtmSource(
                            order.has("note_attributes") ? order.get("note_attributes").toString() : null,
                            order.path("landing_site").asText("")
                    );

                    BigDecimal revenue = new BigDecimal(order.path("total_price").asText("0"));

                    hourTotalRevenue.put(hour, hourTotalRevenue.get(hour).add(revenue));

                    hourMatrix.get(hour).putIfAbsent(source, new GroupData());

                    GroupData sg = hourMatrix.get(hour).get(source);
                    GroupData all = hourMatrix.get(hour).get("ALL");

                    sg.revenue = sg.revenue.add(revenue);
                    all.revenue = all.revenue.add(revenue);

                    sg.orders++;
                    all.orders++;

                    JsonNode items = order.path("line_items");

                    if (items.isArray()) {
                        for (JsonNode item : items) {
                            int qty = item.path("quantity").asInt();
                            String pid = item.path("product_id").asText("");

                            sg.quantity += qty;
                            all.quantity += qty;

                            BigDecimal cost = productCostMap.getOrDefault(pid, BigDecimal.ZERO)
                                    .multiply(BigDecimal.valueOf(qty));

                            sg.productCost = sg.productCost.add(cost);
                            all.productCost = all.productCost.add(cost);
                        }
                    }

                } catch (Exception ignore) {}
            }
        }

        // ---------- FINAL CALCULATION ----------
        for (int h = 0; h <= nowUtc.getHour(); h++) {

            Map<String, GroupData> groups = hourMatrix.get(h);
            BigDecimal totalRev = hourTotalRevenue.get(h);
            BigDecimal hourSpend = hourlyMetaSpend.get(h);

            if (groups.get("ALL").orders == 0 && hourSpend.compareTo(BigDecimal.ZERO) == 0) continue;

            for (Map.Entry<String, GroupData> entry : groups.entrySet()) {

                String source = entry.getKey();
                GroupData data = entry.getValue();

                BigDecimal logistics = costEngineService.calculateLogisticsCost(data.orders, config);
                BigDecimal fee = costEngineService.calculatePaymentFee(data.revenue, config);

                BigDecimal spend;

                if ("ALL".equals(source)) {
                    spend = hourSpend;
                } else if ("Affiliate".equalsIgnoreCase(source)) {
                    spend = costEngineService.calculateAdSpend(source, data.revenue, BigDecimal.ZERO, BigDecimal.ZERO, config);
                } else {
                    spend = totalRev.compareTo(BigDecimal.ZERO) > 0
                            ? data.revenue.divide(totalRev, 4, RoundingMode.HALF_UP).multiply(hourSpend)
                            : BigDecimal.ZERO;
                }

                BigDecimal profit = costEngineService.calculateProfit(
                        data.revenue,
                        data.productCost,
                        logistics,
                        fee,
                        spend
                );

                Optional<HourlyMetric> existing =
                        hourlyMetricRepository.findByUserIdAndDateAndHourAndSource(
                                user.getId(), date, h, source
                        );

                HourlyMetric metric = existing.orElse(new HourlyMetric());

                metric.setUser(user);
                metric.setDate(date);
                metric.setHour(h);
                metric.setSource(source);

                metric.setRevenue(data.revenue);
                metric.setOrders(data.orders);
                metric.setQuantity(data.quantity);
                metric.setProductCost(data.productCost);
                metric.setLogisticsCost(logistics);
                metric.setPaymentFee(fee);
                metric.setAdSpend(spend);
                metric.setProfit(profit);
                metric.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));

                hourlyMetricRepository.save(metric);
            }
        }
    }

    // ---------- META FIX ----------
    private Map<Integer, BigDecimal> fetchMetaSpendHourly(IntegrationMeta meta, String dateStr) {

        Map<Integer, BigDecimal> map = new HashMap<>();

        try {
            String token = aesUtil.decrypt(meta.getAccessToken());

            String url = "https://graph.facebook.com/v19.0/" + meta.getAdAccountId() +
                    "/insights?time_range={'since':'" + dateStr + "','until':'" + dateStr + "'}" +
                    "&fields=spend" +
                    "&breakdowns=hourly_stats_aggregated_by_advertiser_time_zone" +
                    "&access_token=" + token;

            RestTemplate rest = new RestTemplate();

            JsonNode res = rest.getForObject(url, JsonNode.class);

            if (res != null && res.has("data")) {
                for (JsonNode row : res.get("data")) {

                    String time = row.path("hourly_stats_aggregated_by_advertiser_time_zone").asText("");

                    try {
                        int hour = Integer.parseInt(time.split(":")[0]);
                        BigDecimal spend = new BigDecimal(row.path("spend").asText("0"));

                        map.put(hour, spend);

                        log.info("META hour={} spend={}", hour, spend);

                    } catch (Exception ignore) {}
                }
            }

        } catch (Exception e) {
            log.error("Meta API error", e);
        }

        return map;
    }

    private JsonNode fetchShopifyOrders(IntegrationShopify shopify, String token, ZonedDateTime nowUtc) {

        String since = nowUtc.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String url = "https://" + shopify.getStoreUrl() +
                ".myshopify.com/admin/api/2024-01/orders.json?status=any&created_at_min=" + since;

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Shopify-Access-Token", token);

        try {
            ResponseEntity<JsonNode> res =
                    rest.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

            return res.getBody().path("orders");

        } catch (Exception e) {
            log.error("Shopify fetch failed", e);
            return null;
        }
    }

    public String getShopifyAccessToken(IntegrationShopify shopify) {
        return aesUtil.decrypt(shopify.getClientSecret());
    }
}