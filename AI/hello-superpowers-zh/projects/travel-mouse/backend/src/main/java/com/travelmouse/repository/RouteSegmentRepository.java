package com.travelmouse.repository;

import com.travelmouse.entity.RouteSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteSegmentRepository extends JpaRepository<RouteSegment, Long> {
    List<RouteSegment> findByDailyPlanIdOrderByRouteOrder(Long dailyPlanId);
    void deleteByDailyPlanId(Long dailyPlanId);
}
