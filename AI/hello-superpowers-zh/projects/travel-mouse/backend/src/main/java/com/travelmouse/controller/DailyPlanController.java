package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.DailyPlan;
import com.travelmouse.service.DailyPlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans/{planId}/days")
public class DailyPlanController {
    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping("/{dayId}")
    public ApiResponse<DailyPlan> detail(@PathVariable Long planId, @PathVariable Long dayId) {
        return ApiResponse.success(dailyPlanService.findById(dayId));
    }

    @PutMapping("/{dayId}/save")
    public ApiResponse<DailyPlan> saveDay(@PathVariable Long planId, @PathVariable Long dayId) {
        return ApiResponse.success(dailyPlanService.saveDay(dayId));
    }
}
