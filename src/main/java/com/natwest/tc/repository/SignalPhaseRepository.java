package com.natwest.tc.repository;
import com.natwest.tc.entity.SignalPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalPhaseRepository extends JpaRepository<SignalPhase, Integer> {
    List<SignalPhase> findByIsActiveTrue();
}
