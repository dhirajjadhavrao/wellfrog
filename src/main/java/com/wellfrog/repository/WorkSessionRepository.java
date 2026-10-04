package com.wellfrog.repository;

import com.wellfrog.model.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {
    Optional<WorkSession> findByUserIdAndSessionDate(Long userId, LocalDate sessionDate);
    List<WorkSession> findByUserIdOrderBySessionDateDesc(Long userId);
}
