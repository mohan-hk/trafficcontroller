package com.natwest.tc.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String eventType; // e.g., STATE_CHANGE, ERROR, COMMAND

    @Column(length = 500)
    private String details;

    // Constructor for easy logging
    public HistoryEvent(LocalDateTime timestamp, String eventType, String details) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.details = details;
    }
}
