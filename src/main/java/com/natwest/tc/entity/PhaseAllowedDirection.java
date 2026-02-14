package com.natwest.tc.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "phase_allowed_directions")
@Data
public class PhaseAllowedDirection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private SignalPhase phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;
}