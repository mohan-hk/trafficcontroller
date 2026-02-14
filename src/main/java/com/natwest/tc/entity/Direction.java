package com.natwest.tc.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "directions")
@Data
public class Direction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direction_id")
    private Integer directionId;
    @Column(name = "direction_name", nullable = false, unique = true)
    private String directionName;
    private String description;
}