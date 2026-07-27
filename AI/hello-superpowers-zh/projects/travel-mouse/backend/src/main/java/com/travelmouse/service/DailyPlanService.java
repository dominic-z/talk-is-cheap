package com.travelmouse.service;

import com.travelmouse.entity.DailyPlan;
import com.travelmouse.repository.DailyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DailyPlanService {
    private final DailyPlanRepository dailyPlanRepository;

    public DailyPlanService(DailyPlanRepository dailyPlanRepository) {
        this.dailyPlanRepository = dailyPlanRepository;
    }

    public DailyPlan findById(Long id) {
        return dailyPlanRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("单天计划不存在: " + id));
    }

    public List<DailyPlan> findByTravelPlanId(Long travelPlanId) {
        return dailyPlanRepository.findByTravelPlanIdOrderBySortOrder(travelPlanId);
    }

    @Transactional
    public DailyPlan saveDay(Long dayId) {
        DailyPlan plan = findById(dayId);
        plan.setStatus("done");
        return dailyPlanRepository.save(plan);
    }
}
