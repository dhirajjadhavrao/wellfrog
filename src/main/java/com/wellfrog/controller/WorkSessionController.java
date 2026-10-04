package com.wellfrog.controller;

import com.wellfrog.model.WorkSession;
import com.wellfrog.repository.WorkSessionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/work")
public class WorkSessionController {

    private final WorkSessionRepository workSessionRepository;

    public WorkSessionController(WorkSessionRepository workSessionRepository) {
        this.workSessionRepository = workSessionRepository;
    }

    @GetMapping
    public ResponseEntity<?> getWorkSession(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        WorkSession session = workSessionRepository.findByUserIdAndSessionDate(userId, targetDate).orElse(null);
        return ResponseEntity.ok(session != null ? session : Map.of("sessionDate", targetDate, "hoursWorked", 0.0, "tasks", "", "notes", ""));
    }

    @PostMapping
    public ResponseEntity<WorkSession> logWorkSession(Authentication authentication, @RequestBody WorkSession payload) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate targetDate = payload.getSessionDate() != null ? payload.getSessionDate() : LocalDate.now();

        WorkSession session = workSessionRepository.findByUserIdAndSessionDate(userId, targetDate)
                .orElse(new WorkSession(userId, targetDate, 0.0, "", ""));

        session.setHoursWorked(payload.getHoursWorked());
        session.setTasks(payload.getTasks());
        session.setNotes(payload.getNotes());

        return ResponseEntity.ok(workSessionRepository.save(session));
    }
}
