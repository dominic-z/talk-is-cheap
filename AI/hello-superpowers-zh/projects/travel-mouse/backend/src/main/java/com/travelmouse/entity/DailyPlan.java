package com.travelmouse.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_plans")
public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "travel_plan_id", nullable = false)
    private Long travelPlanId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 20)
    private String status = "draft";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTravelPlanId() { return travelPlanId; }
    public void setTravelPlanId(Long travelPlanId) { this.travelPlanId = travelPlanId; }
    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
