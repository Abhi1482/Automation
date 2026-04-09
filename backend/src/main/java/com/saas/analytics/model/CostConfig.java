package com.saas.analytics.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "cost_config")
@Data
public class CostConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "per_order_cost")
    private BigDecimal perOrderCost = new BigDecimal("65.00");

    @Column(name = "payment_fee_percent")
    private BigDecimal paymentFeePercent = new BigDecimal("0.0295");

    @Column(name = "affiliate_percent")
    private BigDecimal affiliatePercent = new BigDecimal("0.2800");

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}
