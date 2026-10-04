package com.wellfrog.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String workoutType; // Gym / Strength, Running, Cardio, Walking, Yoga, HIIT, Rest

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private LocalDate workoutDate;

    private String notes; // Exercises, reps, distance, feeling

    public Workout() {}

    public Workout(Long userId, String workoutType, Integer durationMinutes, LocalDate workoutDate, String notes) {
        this.userId = userId;
        this.workoutType = workoutType;
        this.durationMinutes = durationMinutes;
        this.workoutDate = workoutDate;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public LocalDate getWorkoutDate() { return workoutDate; }
    public void setWorkoutDate(LocalDate workoutDate) { this.workoutDate = workoutDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
