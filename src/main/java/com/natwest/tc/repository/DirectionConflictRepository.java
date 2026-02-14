package com.natwest.tc.repository;

import com.natwest.tc.entity.DirectionConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectionConflictRepository extends JpaRepository<DirectionConflict, Integer> {

    List<DirectionConflict> findByDirection_DirectionId(Integer directionId);

    @Query("SELECT dc FROM DirectionConflict dc WHERE dc.direction.directionId IN :directionIds")
    List<DirectionConflict> findAllConflictsForDirections(@Param("directionIds") List<Integer> directionIds);
}