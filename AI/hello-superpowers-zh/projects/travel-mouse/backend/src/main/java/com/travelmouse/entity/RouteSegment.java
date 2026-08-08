package com.travelmouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "route_segments")
public class RouteSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_plan_id", nullable = false)
    private Long dailyPlanId;

    @Column(name = "from_dest_id", nullable = false)
    private Long fromDestId;

    @Column(name = "to_dest_id", nullable = false)
    private Long toDestId;

    @Column(name = "transport_mode", nullable = false, length = 20)
    private String transportMode;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "distance_meters", nullable = false)
    private Integer distanceMeters;

    @Column(name = "route_order", nullable = false)
    private Integer routeOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDailyPlanId() { return dailyPlanId; }
    public void setDailyPlanId(Long dailyPlanId) { this.dailyPlanId = dailyPlanId; }
    public Long getFromDestId() { return fromDestId; }
    public void setFromDestId(Long fromDestId) { this.fromDestId = fromDestId; }
    public Long getToDestId() { return toDestId; }
    public void setToDestId(Long toDestId) { this.toDestId = toDestId; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }
    public Integer getRouteOrder() { return routeOrder; }
    public void setRouteOrder(Integer routeOrder) { this.routeOrder = routeOrder; }
}
