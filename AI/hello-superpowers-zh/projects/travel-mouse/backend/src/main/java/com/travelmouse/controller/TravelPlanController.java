package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.dto.TravelPlanCreateRequest;
import com.travelmouse.entity.DailyPlan;
import com.travelmouse.entity.TravelPlan;
import com.travelmouse.service.TravelPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class TravelPlanController {
    private final TravelPlanService travelPlanService;

    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @GetMapping
    public ApiResponse<List<TravelPlan>> list() {
        return ApiResponse.success(travelPlanService.findAll());
    }

    @PostMapping
    public ApiResponse<TravelPlan> create(@Valid @RequestBody TravelPlanCreateRequest req) {
        return ApiResponse.success(travelPlanService.create(req.getName(), req.getStartDate(), req.getEndDate()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        TravelPlan plan = travelPlanService.findById(id);
        List<DailyPlan> dailyPlans = travelPlanService.findDailyPlans(id);
        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("dailyPlans", dailyPlans);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<TravelPlan> update(@PathVariable Long id, @Valid @RequestBody TravelPlanCreateRequest req) {
        return ApiResponse.success(travelPlanService.update(id, req.getName(), req.getStartDate(), req.getEndDate()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        travelPlanService.delete(id);
        return ApiResponse.success(null);
    }
}
