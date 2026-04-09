package com.saas.analytics.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "hourly_metrics")
@Data
public class HourlyMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_account_id")
    private IntegrationMeta metaAccount;

    private LocalDate date;
    private Integer hour;
    private String source;

    private BigDecimal revenue = BigDecimal.ZERO;
    private Integer orders = 0;
    private Integer quantity = 0;
    
    @Column(name = "product_cost")
    private BigDecimal productCost = BigDecimal.ZERO;
    
    @Column(name = "logistics_cost")
    private BigDecimal logisticsCost = BigDecimal.ZERO;
    
    @Column(name = "payment_fee")
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(name = "ad_spend")
    private BigDecimal adSpend = BigDecimal.ZERO;
    
    private BigDecimal profit = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
