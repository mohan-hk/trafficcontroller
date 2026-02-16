package com.natwest.tc.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class IntersectionState {
    private String intersectionId = "MAIN_JUNCTION_1";
    private boolean isPaused ;
    private Integer currentPhaseId;
    private LightColor currentPhaseColor = LightColor.RED;
    private LocalDateTime phaseStartTime = LocalDateTime.now();
    private Map<Integer, LightColor> activeGreenDirections = new ConcurrentHashMap<>();
}