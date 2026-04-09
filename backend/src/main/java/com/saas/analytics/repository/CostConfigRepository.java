package com.saas.analytics.repository;

import com.saas.analytics.model.CostConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostConfigRepository extends JpaRepository<CostConfig, Long> {
    Optional<CostConfig> findByUserId(Long userId);
}
