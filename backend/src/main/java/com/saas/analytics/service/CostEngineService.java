package com.saas.analytics.service;

import com.saas.analytics.model.CostConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostEngineService {

    public BigDecimal calculateProportionalSpend(BigDecimal sourceRevenue, BigDecimal totalRevenue, BigDecimal totalMetaSpend) {
        if (totalRevenue == null || totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; 
        }
        if (sourceRevenue == null) sourceRevenue = BigDecimal.ZERO;
        if (totalMetaSpend == null) totalMetaSpend = BigDecimal.ZERO;

        return sourceRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(totalMetaSpend);
    }

    public BigDecimal calculateLogisticsCost(Integer orders, CostConfig costConfig) {
        if (orders == null || orders == 0 || costConfig == null || costConfig.getPerOrderCost() == null) {
            return BigDecimal.ZERO;
        }
        return costConfig.getPerOrderCost().multiply(new BigDecimal(orders));
    }

    public BigDecimal calculatePaymentFee(BigDecimal revenue, CostConfig costConfig) {
        if (revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0 || costConfig == null || costConfig.getPaymentFeePercent() == null) {
            return BigDecimal.ZERO;
        }
        return revenue.multiply(costConfig.getPaymentFeePercent());
    }

    public BigDecimal calculateAdSpend(String source, BigDecimal revenue, BigDecimal totalMetaSpend, BigDecimal totalRevenue, CostConfig config) {
        if ("facebook".equalsIgnoreCase(source) || "SR_facebook".equalsIgnoreCase(source)) {
            return calculateProportionalSpend(revenue, totalRevenue, totalMetaSpend);
        } else if ("Affiliate".equalsIgnoreCase(source)) {
            if (revenue == null || config == null || config.getAffiliatePercent() == null) return BigDecimal.ZERO;
            return revenue.multiply(config.getAffiliatePercent());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateProfit(BigDecimal revenue, BigDecimal productCost, BigDecimal logisticsCost, BigDecimal paymentFee, BigDecimal adSpend) {
        if (revenue == null) revenue = BigDecimal.ZERO;
        if (productCost == null) productCost = BigDecimal.ZERO;
        if (logisticsCost == null) logisticsCost = BigDecimal.ZERO;
        if (paymentFee == null) paymentFee = BigDecimal.ZERO;
        if (adSpend == null) adSpend = BigDecimal.ZERO;

        return revenue.subtract(productCost).subtract(logisticsCost).subtract(paymentFee).subtract(adSpend);
    }
}
