package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.dto.RouteUpdateRequest;
import com.travelmouse.entity.RouteSegment;
import com.travelmouse.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/days/{dayId}/route")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ApiResponse<List<RouteSegment>> getRoute(@PathVariable Long dayId) {
        return ApiResponse.success(routeService.findByDailyPlanId(dayId));
    }

    @PutMapping
    public ApiResponse<List<RouteSegment>> updateRoute(@PathVariable Long dayId,
                                                       @Valid @RequestBody RouteUpdateRequest request) {
        return ApiResponse.success(routeService.updateRoute(dayId, request));
    }
}
