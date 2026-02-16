package com.natwest.tc.controller;

import com.natwest.tc.model.IntersectionState;
import com.natwest.tc.model.TrafficHistory;
import com.natwest.tc.service.TrafficCacheService;
import com.natwest.tc.service.TrafficEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficEngineService engineService;
    private final TrafficCacheService cacheService;

    /**
     * GET /api/traffic/state
     * Returns the current color and active directions.
     */
    @GetMapping("/state")
    public ResponseEntity<IntersectionState> getCurrentState() {
        return ResponseEntity.ok(engineService.getCurrentState());
    }

    /**
     * POST /api/traffic/refresh-cache
     * Reloads data from the DB into the TrafficCacheService.
     */
    @PostMapping("/refresh-cache")
    public ResponseEntity<Map<String, String>> refreshCache() {
        cacheService.reloadCache();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Caches reloaded from database"));
    }

    /**
     * POST /api/traffic/sequence
     * Changes the current operating sequence (e.g., [1, 3, 2]).
     */
    @PostMapping("/sequence")
    public ResponseEntity<String> updateSequence(@RequestBody List<Integer> newSequence) {
        engineService.startCycle(newSequence);
        return ResponseEntity.ok("New sequence started successfully.");
    }

    /**
     * POST /api/traffic/pause
     */
    @PostMapping("/pause")
    public ResponseEntity<String> pause() {
        engineService.pauseSystem();
        return ResponseEntity.ok("System paused at RED.");
    }

    /**
     * POST /api/traffic/resume
     */
    @PostMapping("/resume")
    public ResponseEntity<String> resume() {
        engineService.resumeSystem();
        return ResponseEntity.ok("System resumed.");
    }

    /**
     * GET /api/traffic/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<TrafficHistory>> getHistory() {
        return ResponseEntity.ok(engineService.getHistory());
    }
}