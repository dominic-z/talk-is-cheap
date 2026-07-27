package com.travelmouse.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RouteUpdateRequest {
    @NotNull
    private List<SegmentItem> segments;

    public static class SegmentItem {
        @NotNull private Long fromDestId;
        @NotNull private Long toDestId;
        @NotNull private String transportMode;
        @NotNull private Integer durationMinutes;
        @NotNull private Integer distanceMeters;
        @NotNull private Integer routeOrder;

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

    public List<SegmentItem> getSegments() { return segments; }
    public void setSegments(List<SegmentItem> segments) { this.segments = segments; }
}
