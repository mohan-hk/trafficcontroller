package com.natwest.tc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "direction_conflicts")
@Data
public class DirectionConflict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conflict_id")
    private Integer conflictId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conflicts_with_direction_id", nullable = false)
    private Direction conflictingDirection;

    @Column(name = "conflict_reason")
    private String conflictReason;
}