package com.travelmouse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "destinations")
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_plan_id", nullable = false)
    private Long dailyPlanId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(length = 50)
    private String category;

    @Column(name = "note_text", columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "arrive_time")
    private LocalTime arriveTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "leave_time")
    private LocalTime leaveTime;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "in_route", nullable = false)
    private Boolean inRoute = false;

    @Column(name = "route_order")
    private Integer routeOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDailyPlanId() { return dailyPlanId; }
    public void setDailyPlanId(Long dailyPlanId) { this.dailyPlanId = dailyPlanId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }
    public LocalTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalTime arriveTime) { this.arriveTime = arriveTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalTime leaveTime) { this.leaveTime = leaveTime; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getInRoute() { return inRoute; }
    public void setInRoute(Boolean inRoute) { this.inRoute = inRoute; }
    public Integer getRouteOrder() { return routeOrder; }
    public void setRouteOrder(Integer routeOrder) { this.routeOrder = routeOrder; }
}
