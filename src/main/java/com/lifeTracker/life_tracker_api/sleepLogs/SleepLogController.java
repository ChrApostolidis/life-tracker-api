package com.lifeTracker.life_tracker_api.sleepLogs;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    @GetMapping("/sleep-logs")
    public ResponseEntity<List<SleepLog>> listSleepLogs(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(sleepLogService.list(from, to));
    }

    @PostMapping("/sleep-logs/{date}")
    public ResponseEntity<SleepLog> saveSleepLog(@PathVariable String date, @Valid @RequestBody SleepLogSaveRequest request) {
        return ResponseEntity.ok(sleepLogService.save(date, request));
    }

    @DeleteMapping("/sleep-logs/{date}")
    public ResponseEntity<Void> deleteSleepLog(@PathVariable String date) {
        sleepLogService.delete(date);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sleep-logs/{date}/restore")
    public ResponseEntity<Void> restoreSleepLog(@PathVariable String date) {
        sleepLogService.restore(date);
        return ResponseEntity.noContent().build();
    }
}
