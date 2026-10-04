package com.wellfrog.controller;

import com.wellfrog.model.ActivityLog;
import com.wellfrog.repository.ActivityLogRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogController(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getLogs(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(activityLogRepository.findByUserIdAndLogDate(userId, targetDate));
    }

    @PostMapping
    public ResponseEntity<ActivityLog> logActivity(Authentication authentication, @RequestBody ActivityLog log) {
        Long userId = (Long) authentication.getPrincipal();
        log.setUserId(userId);
        if (log.getLogDate() == null) {
            log.setLogDate(LocalDate.now());
        }
        return ResponseEntity.ok(activityLogRepository.save(log));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLog(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityLog log = activityLogRepository.findById(id).orElse(null);
        if (log != null && log.getUserId().equals(userId)) {
            activityLogRepository.delete(log);
            return ResponseEntity.ok(Map.of("message", "Activity log deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Log not found"));
    }
}
