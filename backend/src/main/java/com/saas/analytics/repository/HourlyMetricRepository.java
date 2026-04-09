package com.saas.analytics.repository;

import com.saas.analytics.model.HourlyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyMetricRepository extends JpaRepository<HourlyMetric, Long> {

    Optional<HourlyMetric> findByUserIdAndMetaAccount_IdAndDateAndHourAndSource(
            Long userId, Long metaAccountId, LocalDate date, Integer hour, String source);

    List<HourlyMetric> findAllByUserIdAndDate(Long userId, LocalDate date);

    List<HourlyMetric> findAllByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    Optional<HourlyMetric> findByUserIdAndDateAndHourAndSource(
    Long userId,
    LocalDate date,
    int hour,
    String source
);

    @Query("SELECT sum(h.adSpend) as totalSpend, sum(h.revenue) as totalRevenue, sum(h.profit) as totalProfit, sum(h.orders) as totalOrders FROM HourlyMetric h WHERE h.user.id = :userId AND h.source = 'ALL'")
    Object[] getKpiTotals(@Param("userId") Long userId);
}
