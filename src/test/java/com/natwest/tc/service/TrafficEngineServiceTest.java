package com.natwest.tc.service;


import com.natwest.tc.entity.Direction;
import com.natwest.tc.entity.DirectionConflict;
import com.natwest.tc.entity.HistoryEvent;
import com.natwest.tc.entity.SignalPhase;
import com.natwest.tc.model.IntersectionState;
import com.natwest.tc.model.LightColor;
import com.natwest.tc.repository.HistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // <--- Add this line
class TrafficEngineServiceTest {

    @Mock
    private TrafficCacheService cacheService;

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private TrafficEngineService trafficEngineService;

    @BeforeEach
    void setUp() {
        // Ensure we start with a clean state before each test
        when(historyRepository.save(any(HistoryEvent.class))).thenReturn(new HistoryEvent());
    }

    @Test
    @DisplayName("Should initialize with GREEN when a new cycle starts")
    void testStartCycle() {
        List<Integer> sequence = Arrays.asList(1, 2);

        trafficEngineService.startCycle(sequence);

        IntersectionState state = trafficEngineService.getCurrentState();
        assertEquals(LightColor.GREEN, state.getCurrentPhaseColor());
        assertEquals(1, state.getCurrentPhaseId());
        assertFalse(state.isPaused());
    }

    @Test
    @DisplayName("Should transition from GREEN to YELLOW after duration expires")
    void testGreenToYellowTransition() {
        // Arrange: Set phase 1 duration to 5 seconds
        SignalPhase phase1 = new SignalPhase();
        phase1.setPhaseId(1);
        phase1.setDurationSeconds(5);
        when(cacheService.getPhase(1)).thenReturn(phase1);
        trafficEngineService.startCycle(Arrays.asList(1, 2));

        // Act: Simulate 5 ticks (seconds)
        for (int i = 0; i < 5; i++) {
            trafficEngineService.trafficTick();
        }

        // Assert
        assertEquals(LightColor.YELLOW, trafficEngineService.getCurrentState().getCurrentPhaseColor());
    }

    @Test
    @DisplayName("Should transition from YELLOW to RED and move to next phase")
    void testYellowToRedToNextPhase() {
        // Arrange: Start in Phase 1 YELLOW
        trafficEngineService.startCycle(Arrays.asList(1, 2));

        // Manual color change for test setup (internal state via ticks)
        SignalPhase phase1 = new SignalPhase();
        phase1.setPhaseId(1);
        phase1.setDurationSeconds(1);
        when(cacheService.getPhase(1)).thenReturn(phase1);

        trafficEngineService.trafficTick(); // Turns Yellow

        // Act: Yellow lasts 3 seconds. Simulate 3 ticks.
        for (int i = 0; i < 3; i++) {
            trafficEngineService.trafficTick();
        }

        // Assert: Should now be Phase 2 GREEN
        assertEquals(2, trafficEngineService.getCurrentState().getCurrentPhaseId());
        assertEquals(LightColor.GREEN, trafficEngineService.getCurrentState().getCurrentPhaseColor());
    }

    @Test
    @DisplayName("Should Pause and turn RED on safety conflict")
    void testSafetyConflictPause() {
        // 1. Setup Phase 1 (Initial State)
        SignalPhase p1 = new SignalPhase();
        p1.setPhaseId(1);
        p1.setDurationSeconds(0); // Trigger immediate transition
        when(cacheService.getPhase(1)).thenReturn(p1);
        when(cacheService.getAllowedDirectionsForPhase(1)).thenReturn(Collections.emptyList());

        // 2. Setup Phase 2 Directions
        Direction north = new Direction();
        north.setDirectionId(101); // Use a distinct ID
        north.setDirectionName("North");

        Direction south = new Direction();
        south.setDirectionId(102); // Use a distinct ID
        south.setDirectionName("South");

        // Phase 2 contains both North and South
        when(cacheService.getAllowedDirectionsForPhase(2)).thenReturn(Arrays.asList(north, south));

        // 3. Create the Conflict: North (101) conflicts with South (102)
        DirectionConflict conflict = new DirectionConflict();
        conflict.setDirection(north);

        // The "conflictingDirection" MUST have ID 102 to match 'south'
        Direction danger = new Direction();
        danger.setDirectionId(102);
        conflict.setConflictingDirection(danger);

        // Stub: When checking conflicts for ID 101, return this conflict
        when(cacheService.getConflictsForDirection(101)).thenReturn(List.of(conflict));

        // 4. Act: Trigger the ticks
        trafficEngineService.startCycle(Arrays.asList(1, 2));

        // We need enough ticks to pass the 3-second Yellow duration
        trafficEngineService.trafficTick(); // Green -> Yellow
        trafficEngineService.trafficTick(); // Yellow (1s)
        trafficEngineService.trafficTick(); // Yellow (2s)
        trafficEngineService.trafficTick(); // Yellow (3s) -> Transitions to RED -> Checks Phase 2

        // 5. Assert
        IntersectionState state = trafficEngineService.getCurrentState();
        assertTrue(state.isPaused(), "Engine should be paused due to Phase 2 conflict");
        assertEquals(LightColor.RED, state.getCurrentPhaseColor());
    }
    @Test
    @DisplayName("Should not process ticks when system is paused")
    void testNoTickWhenPaused() {
        trafficEngineService.startCycle(Arrays.asList(1, 2));
        trafficEngineService.pauseSystem();

        // Act: Run a tick
        trafficEngineService.trafficTick();

        // Assert: Time in state should not have progressed logically toward color change
        assertTrue(trafficEngineService.getCurrentState().isPaused());
        assertEquals(LightColor.RED, trafficEngineService.getCurrentState().getCurrentPhaseColor());
    }
}