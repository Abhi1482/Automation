package com.saas.analytics.repository;

import com.saas.analytics.model.IntegrationMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationMetaRepository extends JpaRepository<IntegrationMeta, Long> {
    List<IntegrationMeta> findAllByUserId(Long userId);
}
