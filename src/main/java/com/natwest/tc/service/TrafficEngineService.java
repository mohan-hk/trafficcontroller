package com.natwest.tc.service;

import com.natwest.tc.entity.Direction;
import com.natwest.tc.entity.DirectionConflict;
import com.natwest.tc.entity.HistoryEvent;
import com.natwest.tc.entity.SignalPhase;
import com.natwest.tc.model.IntersectionState;
import com.natwest.tc.model.LightColor;
import com.natwest.tc.model.TrafficHistory;
import com.natwest.tc.repository.HistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficEngineService {

    private final TrafficCacheService cacheService;
    private final HistoryRepository historyRepository;

    private boolean isPaused = false;
    private List<Integer> sequence = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
    private int currentSequenceIndex = 0;

    private LightColor currentColor = LightColor.RED;
    private int secondsInCurrentState = 0;

    @PostConstruct
    public void init() {
        log.info("TrafficEngineService initialized. Sequence: {}, Initial State: {}", sequence, currentColor);
    }

    /**
     * Heartbeat method: Executes every 1000ms (1 second).
     */
    @Scheduled(fixedRate = 1000)
    public void trafficTick() {
        // If the system is paused or no sequence exists, skip processing.
        if (isPaused || sequence.isEmpty()) {
            return;
        }

        secondsInCurrentState++;

        // Defensive check for index safety
        if (currentSequenceIndex >= sequence.size()) {
            currentSequenceIndex = 0;
        }

        Integer currentPhaseId = sequence.get(currentSequenceIndex);
        SignalPhase currentPhase = cacheService.getPhase(currentPhaseId);

        // Get green duration from cache or default to 60 seconds
        int greenDuration = (currentPhase != null) ? currentPhase.getDurationSeconds() : 60;

        // Manage state transitions based on time elapsed
        if (currentColor == LightColor.GREEN && secondsInCurrentState >= greenDuration) {
            changeColor(LightColor.YELLOW);
        } else if (currentColor == LightColor.YELLOW && secondsInCurrentState >= 3) {
            changeColor(LightColor.RED);
            moveToNextPhase();
        }
    }

    private void changeColor(LightColor newColor) {
        this.currentColor = newColor;
        this.secondsInCurrentState = 0;

        String phaseStr = sequence.isEmpty() ? "NONE" : String.valueOf(sequence.get(currentSequenceIndex));
        logEvent("STATE_CHANGE", "Phase " + phaseStr + " turned " + newColor);
    }

    private void moveToNextPhase() {
        if (sequence.isEmpty()) return;

        int nextIndex = (currentSequenceIndex + 1) % sequence.size();
        Integer nextPhaseId = sequence.get(nextIndex);

        List<Direction> upcomingDirections = cacheService.getAllowedDirectionsForPhase(nextPhaseId);

        // Safety Validation: Ensure no conflicts exist before turning GREEN
        if (isSafeToTurnGreen(upcomingDirections)) {
            this.currentSequenceIndex = nextIndex;
            changeColor(LightColor.GREEN);
        } else {
            pauseSystem();
            logEvent("ERROR", "Safety violation detected for Phase " + nextPhaseId + ". System locked to RED.");
        }
    }

    private boolean isSafeToTurnGreen(List<Direction> directions) {
        if (directions == null || directions.isEmpty()) return true;

        Set<Integer> upcomingIds = new HashSet<>();
        for (Direction d : directions) {
            upcomingIds.add(d.getDirectionId());
        }

        for (Direction d : directions) {
            List<DirectionConflict> conflicts = cacheService.getConflictsForDirection(d.getDirectionId());
            for (DirectionConflict conflict : conflicts) {
                if (upcomingIds.contains(conflict.getConflictingDirection().getDirectionId())) {
                    return false; // Found a collision risk
                }
            }
        }
        return true;
    }

    public void pauseSystem() {
        this.isPaused = true;
        this.currentColor = LightColor.RED;
        logEvent("COMMAND", "System Paused. All lights forced to RED.");
    }

    public void resumeSystem() {
        if (sequence.isEmpty()) {
            logEvent("ERROR", "Cannot resume: sequence is empty.");
            return;
        }
        this.isPaused = false;
        logEvent("COMMAND", "System Resumed.");
    }

    public void startCycle(List<Integer> newSequence) {
        if (newSequence == null || newSequence.isEmpty()) {
            this.sequence = new ArrayList<>();
            pauseSystem();
            logEvent("ERROR", "Empty sequence provided. System halted.");
            return;
        }

        this.sequence = new ArrayList<>(newSequence);
        this.currentSequenceIndex = 0;
        this.isPaused = false;
        changeColor(LightColor.GREEN);
        logEvent("COMMAND", "New sequence started: " + newSequence);
    }

    private void logEvent(String type, String details) {
        try {
            HistoryEvent event = new HistoryEvent(LocalDateTime.now(), type, details);
            historyRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to persist history event to database: {}", e.getMessage());
        }
        log.info("[{}] {}", type, details);
    }

    public IntersectionState getCurrentState() {
        IntersectionState state = new IntersectionState();
        state.setIntersectionId("MAIN_JUNCTION_01");
        state.setPaused(isPaused);

        Integer currentPhaseId = sequence.isEmpty() ? 0 : sequence.get(currentSequenceIndex);
        state.setCurrentPhaseId(currentPhaseId);
        state.setCurrentPhaseColor(currentColor);

        Map<Integer, LightColor> activeDirs = new HashMap<>();
        if (currentColor != LightColor.RED && !sequence.isEmpty()) {
            List<Direction> allowed = cacheService.getAllowedDirectionsForPhase(currentPhaseId);
            for (Direction d : allowed) {
                activeDirs.put(d.getDirectionId(), currentColor);
            }
        }
        state.setActiveGreenDirections(Collections.unmodifiableMap(activeDirs));

        return state;
    }

    public List<TrafficHistory> getHistory() {

        return historyRepository.findByOrderByTimestampDesc().stream().map(
                history ->
                        new TrafficHistory(history.getTimestamp(), history.getEventType(), history.getDetails())
        ).collect(toList());

    }
}