package com.natwest.tc.controller;


import com.natwest.tc.model.IntersectionState;
import com.natwest.tc.model.LightColor;
import com.natwest.tc.model.TrafficHistory;
import com.natwest.tc.service.TrafficCacheService;
import com.natwest.tc.service.TrafficEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrafficController.class)
class TrafficControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrafficEngineService engineService;

    @MockBean
    private TrafficCacheService cacheService;

    @Test
    @DisplayName("GET /api/traffic/state - Should return current intersection status")
    void shouldReturnCurrentState() throws Exception {
        IntersectionState mockState = new IntersectionState();
        mockState.setCurrentPhaseId(1);
        mockState.setCurrentPhaseColor(LightColor.GREEN);
        mockState.setActiveGreenDirections(Map.of(101, LightColor.GREEN));

        when(engineService.getCurrentState()).thenReturn(mockState);

        mockMvc.perform(get("/api/traffic/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhaseId").value(1))
                .andExpect(jsonPath("$.currentPhaseColor").value("GREEN"))
                .andExpect(jsonPath("$.activeGreenDirections['101']").value("GREEN"));
    }

    @Test
    @DisplayName("POST /api/traffic/refresh-cache - Should trigger reload logic")
    void shouldRefreshCache() throws Exception {
        mockMvc.perform(post("/api/traffic/refresh-cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());

        verify(cacheService, times(1)).reloadCache();
    }

    @Test
    @DisplayName("POST /api/traffic/sequence - Should update engine with new phase list")
    void shouldUpdateSequence() throws Exception {
        List<Integer> newSequence = Arrays.asList(1, 3, 2);
        String jsonBody = "[1, 3, 2]";

        mockMvc.perform(post("/api/traffic/sequence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("New sequence started successfully."));

        verify(engineService, times(1)).startCycle(newSequence);
    }

    @Test
    @DisplayName("POST /api/traffic/pause - Should stop the engine")
    void shouldPauseSystem() throws Exception {
        mockMvc.perform(post("/api/traffic/pause"))
                .andExpect(status().isOk())
                .andExpect(content().string("System paused at RED."));

        verify(engineService, times(1)).pauseSystem();
    }

    @Test
    @DisplayName("POST /api/traffic/resume - Should restart the engine")
    void shouldResumeSystem() throws Exception {
        mockMvc.perform(post("/api/traffic/resume"))
                .andExpect(status().isOk())
                .andExpect(content().string("System resumed."));

        verify(engineService, times(1)).resumeSystem();
    }

    @Test
    @DisplayName("GET /api/traffic/history - Should return list of events")
    void shouldReturnHistory() throws Exception {
        TrafficHistory event = new TrafficHistory(LocalDateTime.now(), "STATE_CHANGE", "Phase 1 turned GREEN");
        when(engineService.getHistory()).thenReturn(List.of(event));

        mockMvc.perform(get("/api/traffic/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("STATE_CHANGE"))
                .andExpect(jsonPath("$[0].details").value("Phase 1 turned GREEN"));
    }
}