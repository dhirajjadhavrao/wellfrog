package com.wellfrog.controller;

import com.wellfrog.model.Activity;
import com.wellfrog.repository.ActivityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository activityRepository;

    public ActivityController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(activityRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(Authentication authentication, @RequestBody Activity activity) {
        Long userId = (Long) authentication.getPrincipal();
        activity.setUserId(userId);
        if (activity.getCategoryType() == null || activity.getCategoryType().isBlank()) {
            activity.setCategoryType("CUSTOM");
        }
        if (activity.getUnit() == null || activity.getUnit().isBlank()) {
            activity.setUnit("MINUTES");
        }
        if (activity.getColor() == null || activity.getColor().isBlank()) {
            activity.setColor("#386641");
        }
        return ResponseEntity.ok(activityRepository.save(activity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity != null && activity.getUserId().equals(userId)) {
            // Delete sub-activities if this is a parent activity
            List<Activity> subActivities = activityRepository.findByUserIdAndParentId(userId, id);
            activityRepository.deleteAll(subActivities);
            activityRepository.delete(activity);
            return ResponseEntity.ok(Map.of("message", "Activity and sub-activities deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Activity not found"));
    }
}
