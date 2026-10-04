package com.wellfrog.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_sessions")
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private Double hoursWorked; // e.g. 7.5

    @Column(columnDefinition = "TEXT")
    private String tasks; // Multiline or JSON list of tasks worked on

    private String notes;

    public WorkSession() {}

    public WorkSession(Long userId, LocalDate sessionDate, Double hoursWorked, String tasks, String notes) {
        this.userId = userId;
        this.sessionDate = sessionDate;
        this.hoursWorked = hoursWorked;
        this.tasks = tasks;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }

    public String getTasks() { return tasks; }
    public void setTasks(String tasks) { this.tasks = tasks; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
