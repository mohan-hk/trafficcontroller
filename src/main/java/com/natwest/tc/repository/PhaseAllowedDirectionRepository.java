package com.natwest.tc.repository;

import com.natwest.tc.entity.PhaseAllowedDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhaseAllowedDirectionRepository extends JpaRepository<PhaseAllowedDirection, Integer> {
    // Fetches all the green lights for a specific phase
    List<PhaseAllowedDirection> findByPhase_PhaseId(Integer phaseId);
}
