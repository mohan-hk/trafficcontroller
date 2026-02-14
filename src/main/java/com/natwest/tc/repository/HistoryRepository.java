package com.natwest.tc.repository;

import com.natwest.tc.entity.HistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEvent, Long> {
    List<HistoryEvent> findByOrderByTimestampDesc();
}