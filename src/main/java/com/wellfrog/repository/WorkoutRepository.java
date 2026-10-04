package com.wellfrog.repository;

import com.wellfrog.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);
    List<Workout> findByUserIdOrderByWorkoutDateDesc(Long userId);
}
