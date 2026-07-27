package com.travelmouse.service;

import com.travelmouse.dto.RouteUpdateRequest;
import com.travelmouse.entity.RouteSegment;
import com.travelmouse.repository.RouteSegmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {
    private final RouteSegmentRepository routeSegmentRepository;
    private final DailyPlanService dailyPlanService;

    public RouteService(RouteSegmentRepository routeSegmentRepository,
                        DailyPlanService dailyPlanService) {
        this.routeSegmentRepository = routeSegmentRepository;
        this.dailyPlanService = dailyPlanService;
    }

    public List<RouteSegment> findByDailyPlanId(Long dailyPlanId) {
        return routeSegmentRepository.findByDailyPlanIdOrderByRouteOrder(dailyPlanId);
    }

    @Transactional
    public List<RouteSegment> updateRoute(Long dailyPlanId, RouteUpdateRequest request) {
        dailyPlanService.findById(dailyPlanId); // 验证存在
        routeSegmentRepository.deleteByDailyPlanId(dailyPlanId);

        List<RouteSegment> segments = new ArrayList<>();
        for (RouteUpdateRequest.SegmentItem item : request.getSegments()) {
            RouteSegment seg = new RouteSegment();
            seg.setDailyPlanId(dailyPlanId);
            seg.setFromDestId(item.getFromDestId());
            seg.setToDestId(item.getToDestId());
            seg.setTransportMode(item.getTransportMode());
            seg.setDurationMinutes(item.getDurationMinutes());
            seg.setDistanceMeters(item.getDistanceMeters());
            seg.setRouteOrder(item.getRouteOrder());
            segments.add(routeSegmentRepository.save(seg));
        }
        return segments;
    }
}
