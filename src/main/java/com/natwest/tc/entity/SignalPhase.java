package com.natwest.tc.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "signal_phases")
@Data
public class SignalPhase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phase_id")
    private Integer phaseId;
    @Column(name = "phase_name", nullable = false)
    private String phaseName;
    private String description;
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    @Column(name = "is_active")
    private Boolean isActive;
}