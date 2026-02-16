package com.natwest.tc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrafficHistory {
    private LocalDateTime timestamp;
    private String eventType;
    private String details;
}