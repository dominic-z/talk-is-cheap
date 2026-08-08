package com.travelmouse.repository;

import com.travelmouse.entity.DailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    List<DailyPlan> findByTravelPlanIdOrderBySortOrder(Long travelPlanId);
    void deleteByTravelPlanId(Long travelPlanId);
}
