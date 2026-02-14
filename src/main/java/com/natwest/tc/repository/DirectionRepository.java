package com.natwest.tc.repository;

import com.natwest.tc.entity.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Integer> {
    Optional<Direction> findByDirectionName(String directionName);
}