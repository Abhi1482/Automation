package com.saas.analytics.repository;

import com.saas.analytics.model.IntegrationShopify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationShopifyRepository extends JpaRepository<IntegrationShopify, Long> {
    List<IntegrationShopify> findAllByUserId(Long userId);
}
