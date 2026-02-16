package com.natwest.tc.stepdefs;

import com.natwest.tc.entity.Direction;
import com.natwest.tc.entity.DirectionConflict;
import com.natwest.tc.entity.SignalPhase;
import com.natwest.tc.model.LightColor;
import com.natwest.tc.repository.HistoryRepository;
import com.natwest.tc.service.TrafficCacheService;
import com.natwest.tc.service.TrafficEngineService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrafficStepDefinitions {

    // Mocking the dependencies to isolate the Engine logic
    private final TrafficCacheService cacheService = mock(TrafficCacheService.class);
    private final HistoryRepository historyRepository = mock(HistoryRepository.class);

    // Injecting mocks into the service
    private final TrafficEngineService engineService = new TrafficEngineService(cacheService, historyRepository);

    // --- SETUP STEPS ---

    @Given("the traffic engine is started with sequence {string}")
    public void startEngine(String seq) {
        List<Integer> list = Arrays.stream(seq.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        engineService.startCycle(list);
    }

    @Given("Phase {int} is currently {string}")
    public void phase_is_currently(Integer phaseId, String colorStr) {
        LightColor expectedColor = LightColor.valueOf(colorStr.toUpperCase());
        assertEquals(expectedColor, engineService.getCurrentState().getCurrentPhaseColor());
    }

    @Given("Phase {int} duration is {int} seconds")
    public void setPhaseDuration(int id, int seconds) {
        SignalPhase phase = new SignalPhase();
        phase.setPhaseId(id);
        phase.setDurationSeconds(seconds);

        // Using lenient to prevent Mockito from failing if a test pauses before calling these
        lenient().when(cacheService.getPhase(id)).thenReturn(phase);
        lenient().when(cacheService.getAllowedDirectionsForPhase(id)).thenReturn(Collections.emptyList());
    }

    @Given("Phase {int} has conflicting directions {string} and {string}")
    public void setupConflict(int phaseId, String dirName1, String dirName2) {
        Direction d1 = new Direction();
        d1.setDirectionId(101);
        d1.setDirectionName(dirName1);

        Direction d2 = new Direction();
        d2.setDirectionId(102);
        d2.setDirectionName(dirName2);

        // Tell cache what directions are in this phase
        when(cacheService.getAllowedDirectionsForPhase(phaseId)).thenReturn(Arrays.asList(d1, d2));

        // Define the conflict
        DirectionConflict conflict = new DirectionConflict();
        conflict.setDirection(d1);

        Direction conflictingDir = new Direction();
        conflictingDir.setDirectionId(102);
        conflict.setConflictingDirection(conflictingDir);

        when(cacheService.getConflictsForDirection(101)).thenReturn(List.of(conflict));
    }

    @Given("the traffic engine is transitioning to Phase {int}")
    public void the_traffic_engine_is_transitioning_to_phase(Integer phaseId) {
        // Setup Phase 1 to end immediately
        SignalPhase p1 = new SignalPhase();
        p1.setPhaseId(1);
        p1.setDurationSeconds(0);
        when(cacheService.getPhase(1)).thenReturn(p1);
        when(cacheService.getAllowedDirectionsForPhase(1)).thenReturn(Collections.emptyList());

        engineService.startCycle(List.of(1, phaseId));

        // Tick through Phase 1 states to reach the transition point
        engineService.trafficTick(); // To Yellow
        engineService.trafficTick(); // Yellow 1
        engineService.trafficTick(); // Yellow 2
        engineService.trafficTick(); // Yellow 3 -> Ready to transition to Phase 2
    }

    // --- ACTION STEPS ---

    @When("{int} seconds pass")
    public void secondsPass(int seconds) {
        for (int i = 0; i < seconds; i++) {
            engineService.trafficTick();
        }
    }

    @When("the safety check is performed")
    public void the_safety_check_is_performed() {
        // Triggers the state change logic where isSafeToTurnGreen is evaluated
        engineService.trafficTick();
    }

    // --- ASSERTION STEPS ---

    @Then("the light for Phase {int} should turn {string}")
    public void checkColor(int id, String color) {
        assertEquals(LightColor.valueOf(color.toUpperCase()),
                engineService.getCurrentState().getCurrentPhaseColor());
    }

    @Then("the system should {string}")
    public void verifySystemStatus(String status) {
        if ("PAUSE".equalsIgnoreCase(status)) {
            assertTrue(engineService.getCurrentState().isPaused());
        } else {
            assertFalse(engineService.getCurrentState().isPaused());
        }
    }

    @Then("all lights should be {string}")
    public void all_lights_should_be(String colorStr) {
        LightColor expected = LightColor.valueOf(colorStr.toUpperCase());
        assertEquals(expected, engineService.getCurrentState().getCurrentPhaseColor());
    }
}