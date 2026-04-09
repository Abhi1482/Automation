package com.saas.analytics.controller;

import com.saas.analytics.model.HourlyMetric;
import com.saas.analytics.model.User;
import com.saas.analytics.repository.HourlyMetricRepository;
import com.saas.analytics.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final HourlyMetricRepository hourlyMetricRepository;
    private final UserRepository userRepository;

    public MetricsController(HourlyMetricRepository hourlyMetricRepository, UserRepository userRepository) {
        this.hourlyMetricRepository = hourlyMetricRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/hourly")
    public ResponseEntity<?> getHourlyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String channel) {
        
        User user = getAuthenticatedUser();
        List<HourlyMetric> metrics = hourlyMetricRepository.findAllByUserIdAndDate(user.getId(), date);
        
        if (channel != null && !channel.isEmpty()) {
            metrics = metrics.stream()
                .filter(m -> m.getSource() != null && m.getSource().equalsIgnoreCase(channel))
                .collect(Collectors.toList());
        } else {
            metrics = metrics.stream()
                .filter(m -> m.getSource() != null && m.getSource().equalsIgnoreCase("ALL"))
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/compare")
    public ResponseEntity<?> getComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        
        User user = getAuthenticatedUser();
        List<HourlyMetric> metrics1 = hourlyMetricRepository.findAllByUserIdAndDate(user.getId(), date1);
        List<HourlyMetric> metrics2 = hourlyMetricRepository.findAllByUserIdAndDate(user.getId(), date2);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("date1", metrics1);
        comparison.put("date2", metrics2);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/kpi")
    public ResponseEntity<?> getKpi() {
        User user = getAuthenticatedUser();
        Object[] totals = hourlyMetricRepository.getKpiTotals(user.getId());
        Map<String, Object> response = new HashMap<>();
        
        if (totals != null && totals.length > 0 && totals[0] != null && ((Object[])totals[0])[0] != null) {
            Object[] row = (Object[]) totals[0];
            response.put("totalSpend", row[0]);
            response.put("totalRevenue", row[1]);
            response.put("totalProfit", row[2]);
            
            Number spend = (Number) row[0];
            Number rev = (Number) row[1];
            Number orders = (Number) row[3];
            
            double dSpend = spend != null ? spend.doubleValue() : 0;
            double dRev = rev != null ? rev.doubleValue() : 0;
            double dOrders = orders != null ? orders.doubleValue() : 0;

            response.put("avgCAC", dOrders > 0 ? dSpend / dOrders : 0);
            response.put("avgROAS", dSpend > 0 ? dRev / dSpend : 0);
        } else {
            response.put("totalSpend", 0);
            response.put("totalRevenue", 0);
            response.put("totalProfit", 0);
            response.put("avgCAC", 0);
            response.put("avgROAS", 0);
        }
        return ResponseEntity.ok(response);
    }
}
