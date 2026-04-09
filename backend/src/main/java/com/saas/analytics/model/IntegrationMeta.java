package com.saas.analytics.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Entity
@Table(name = "integrations_meta")
@Data
public class IntegrationMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "ad_account_id")
    private String adAccountId;

    @Column(name = "access_token")
    private String accessToken;

    private String currency;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}
