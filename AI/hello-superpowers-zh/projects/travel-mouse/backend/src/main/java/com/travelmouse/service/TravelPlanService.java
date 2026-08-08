package com.travelmouse.service;

import com.travelmouse.entity.DailyPlan;
import com.travelmouse.entity.TravelPlan;
import com.travelmouse.repository.DailyPlanRepository;
import com.travelmouse.repository.DestinationRepository;
import com.travelmouse.repository.DestinationImageRepository;
import com.travelmouse.repository.RouteSegmentRepository;
import com.travelmouse.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TravelPlanService {
    private final TravelPlanRepository travelPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DestinationRepository destinationRepository;
    private final DestinationImageRepository imageRepository;
    private final RouteSegmentRepository routeSegmentRepository;

    public TravelPlanService(TravelPlanRepository travelPlanRepository,
                             DailyPlanRepository dailyPlanRepository,
                             DestinationRepository destinationRepository,
                             DestinationImageRepository imageRepository,
                             RouteSegmentRepository routeSegmentRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.destinationRepository = destinationRepository;
        this.imageRepository = imageRepository;
        this.routeSegmentRepository = routeSegmentRepository;
    }

    public List<TravelPlan> findAll() {
        return travelPlanRepository.findAll();
    }

    public TravelPlan findById(Long id) {
        return travelPlanRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("旅行计划不存在: " + id));
    }

    public List<DailyPlan> findDailyPlans(Long travelPlanId) {
        return dailyPlanRepository.findByTravelPlanIdOrderBySortOrder(travelPlanId);
    }

    @Transactional
    public TravelPlan create(String name, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于起始日期");
        }

        TravelPlan plan = new TravelPlan();
        plan.setName(name);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan = travelPlanRepository.save(plan);

        int order = 1;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DailyPlan daily = new DailyPlan();
            daily.setTravelPlanId(plan.getId());
            daily.setPlanDate(d);
            daily.setSortOrder(order++);
            daily.setStatus("draft");
            dailyPlanRepository.save(daily);
        }

        return plan;
    }

    @Transactional
    public TravelPlan update(Long id, String name, LocalDate startDate, LocalDate endDate) {
        TravelPlan plan = findById(id);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于起始日期");
        }
        plan.setName(name);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        return travelPlanRepository.save(plan);
    }

    @Transactional
    public void delete(Long id) {
        TravelPlan plan = findById(id);

        // 业务层级联删除：先删子表数据
        List<DailyPlan> dailyPlans = dailyPlanRepository.findByTravelPlanIdOrderBySortOrder(id);
        for (DailyPlan daily : dailyPlans) {
            // 删除该天所有目的地的图片
            destinationRepository.findByDailyPlanIdOrderBySortOrder(daily.getId())
                    .forEach(dest -> imageRepository.deleteByDestinationId(dest.getId()));
            // 删除该天的路线段
            routeSegmentRepository.deleteByDailyPlanId(daily.getId());
            // 删除该天的目的地
            destinationRepository.deleteByDailyPlanId(daily.getId());
        }
        // 删除所有天
        dailyPlanRepository.deleteByTravelPlanId(id);
        // 删除计划本身
        travelPlanRepository.delete(plan);
    }
}
