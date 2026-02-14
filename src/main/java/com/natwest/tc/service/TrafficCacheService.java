package com.natwest.tc.service;

import com.natwest.tc.entity.Direction;
import com.natwest.tc.entity.DirectionConflict;
import com.natwest.tc.entity.SignalPhase;
import com.natwest.tc.repository.DirectionConflictRepository;
import com.natwest.tc.repository.DirectionRepository;
import com.natwest.tc.repository.PhaseAllowedDirectionRepository;
import com.natwest.tc.repository.SignalPhaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficCacheService {

    private final SignalPhaseRepository phaseRepository;
    private final DirectionRepository directionRepository;
    private final DirectionConflictRepository conflictRepository;
    private final PhaseAllowedDirectionRepository phaseDirectionRepository;

    // Immutable Caches
    private Map<Integer, SignalPhase> phaseCache = Map.of();
    private Map<Integer, Direction> directionCache = Map.of();
    private Map<Integer, List<Direction>> phaseDirectionCache = Map.of();
    private Map<Integer, List<DirectionConflict>> conflictCache = Map.of();

    @PostConstruct
    public void init() {
        reloadCache();
    }

    public void reloadCache() {
        log.info("Starting traffic rules cache reload...");

        this.phaseCache = phaseRepository.findAll().stream()
                .collect(collectingAndThen(
                        toMap(SignalPhase::getPhaseId, Function.identity()),
                        Map::copyOf
                ));

        this.directionCache = directionRepository.findAll().stream()
                .collect(collectingAndThen(
                        toMap(Direction::getDirectionId, Function.identity()),
                        Map::copyOf
                ));

        this.phaseDirectionCache = phaseDirectionRepository.findAll().stream()
                .collect(groupingBy(
                        pd -> pd.getPhase().getPhaseId(),
                        collectingAndThen(
                                mapping(pd -> directionCache.get(pd.getDirection().getDirectionId()), toList()),
                                list -> List.copyOf(list.stream().filter(Objects::nonNull).toList())
                        )
                ));

        this.conflictCache = conflictRepository.findAll().stream()
                .collect(groupingBy(
                        c -> c.getDirection().getDirectionId(),
                        collectingAndThen(toList(), List::copyOf)
                ));

        log.info("Cache reload complete. Phases: {}, Conflicts: {}", phaseCache.size(), conflictCache.size());
    }

    public SignalPhase getPhase(Integer phaseId) {
        return phaseCache.get(phaseId);
    }

    public List<Direction> getAllowedDirectionsForPhase(Integer phaseId) {
        return phaseDirectionCache.getOrDefault(phaseId, List.of());
    }

    public List<DirectionConflict> getConflictsForDirection(Integer directionId) {
        return conflictCache.getOrDefault(directionId, List.of());
    }
}