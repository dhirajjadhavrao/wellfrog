package com.wellfrog.controller;

import com.wellfrog.model.Workout;
import com.wellfrog.repository.WorkoutRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutRepository workoutRepository;

    public WorkoutController(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getWorkouts(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) authentication.getPrincipal();
        if (date != null) {
            return ResponseEntity.ok(workoutRepository.findByUserIdAndWorkoutDate(userId, date));
        }
        return ResponseEntity.ok(workoutRepository.findByUserIdOrderByWorkoutDateDesc(userId));
    }

    @PostMapping
    public ResponseEntity<Workout> logWorkout(Authentication authentication, @RequestBody Workout workout) {
        Long userId = (Long) authentication.getPrincipal();
        workout.setUserId(userId);
        if (workout.getWorkoutDate() == null) {
            workout.setWorkoutDate(LocalDate.now());
        }
        return ResponseEntity.ok(workoutRepository.save(workout));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        Workout workout = workoutRepository.findById(id).orElse(null);
        if (workout != null && workout.getUserId().equals(userId)) {
            workoutRepository.delete(workout);
            return ResponseEntity.ok(Map.of("message", "Workout deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Workout not found"));
    }
}
