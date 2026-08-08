package com.travelmouse.repository;

import com.travelmouse.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByDailyPlanIdOrderBySortOrder(Long dailyPlanId);
    List<Destination> findByDailyPlanIdAndInRouteTrueOrderByRouteOrder(Long dailyPlanId);
    void deleteByDailyPlanId(Long dailyPlanId);
}
