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
    private final com.wellfrog.service.UserService userService;
    private final com.wellfrog.repository.ActivityLogRepository activityLogRepository;

    public ActivityController(ActivityRepository activityRepository,
                              com.wellfrog.service.UserService userService,
                              com.wellfrog.repository.ActivityLogRepository activityLogRepository) {
        this.activityRepository = activityRepository;
        this.userService = userService;
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.ensureDefaultActivitiesForUser(userId);
        return ResponseEntity.ok(activityRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(Authentication authentication, @RequestBody Activity activity) {
        Long userId = (Long) authentication.getPrincipal();
        activity.setUserId(userId);
        if (activity.getCategoryType() == null || activity.getCategoryType().isBlank()) {
            activity.setCategoryType(activity.getParentId() != null ? "SUB_ACTIVITY" : "CUSTOM");
        }
        if (activity.getUnit() == null || activity.getUnit().isBlank()) {
            activity.setUnit("MINUTES");
        }
        if (activity.getColor() == null || activity.getColor().isBlank()) {
            activity.setColor("#386641");
        }
        activity.setActive(true);
        return ResponseEntity.ok(activityRepository.save(activity));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActivityActive(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Boolean> body) {
        Long userId = (Long) authentication.getPrincipal();
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity != null && activity.getUserId().equals(userId)) {
            boolean newActive = (body != null && body.containsKey("active"))
                    ? body.get("active")
                    : !(activity.getActive() != null ? activity.getActive() : true);
            activity.setActive(newActive);
            activityRepository.save(activity);

            // Also synchronize child sub-activities with root status
            if (activity.getParentId() == null) {
                List<Activity> subActivities = activityRepository.findByUserIdAndParentId(userId, id);
                for (Activity sub : subActivities) {
                    sub.setActive(newActive);
                }
                activityRepository.saveAll(subActivities);
            }

            return ResponseEntity.ok(activity);
        }
        return ResponseEntity.status(404).body(Map.of("error", "Activity not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrDeactivateActivity(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean permanent) {
        Long userId = (Long) authentication.getPrincipal();
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity != null && activity.getUserId().equals(userId)) {
            if (permanent) {
                // Permanently delete custom activity, sub-activities and custom activity logs
                List<Activity> subActivities = activityRepository.findByUserIdAndParentId(userId, id);
                for (Activity sub : subActivities) {
                    activityLogRepository.deleteByUserIdAndActivityId(userId, sub.getId());
                }
                activityRepository.deleteAll(subActivities);
                activityLogRepository.deleteByUserIdAndActivityId(userId, id);
                activityRepository.delete(activity);
                return ResponseEntity.ok(Map.of("message", "Activity permanently deleted", "active", false));
            } else {
                // Soft deactivate: hide from dashboard, preserve all data!
                activity.setActive(false);
                activityRepository.save(activity);
                if (activity.getParentId() == null) {
                    List<Activity> subActivities = activityRepository.findByUserIdAndParentId(userId, id);
                    for (Activity sub : subActivities) {
                        sub.setActive(false);
                    }
                    activityRepository.saveAll(subActivities);
                }
                return ResponseEntity.ok(Map.of("message", "Activity deactivated and hidden from dashboard", "active", false));
            }
        }
        return ResponseEntity.status(404).body(Map.of("error", "Activity not found"));
    }
}
