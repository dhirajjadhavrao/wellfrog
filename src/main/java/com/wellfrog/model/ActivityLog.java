package com.wellfrog.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long activityId;

    private Long subActivityId;

    @Column(nullable = false)
    private LocalDate logDate;

    private Double numericValue; // duration in minutes, reps, pages, count, etc.

    private String textValue; // note, checklist, details

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public ActivityLog() {}

    public ActivityLog(Long userId, Long activityId, Long subActivityId, LocalDate logDate, Double numericValue, String textValue) {
        this.userId = userId;
        this.activityId = activityId;
        this.subActivityId = subActivityId;
        this.logDate = logDate;
        this.numericValue = numericValue;
        this.textValue = textValue;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Long getSubActivityId() { return subActivityId; }
    public void setSubActivityId(Long subActivityId) { this.subActivityId = subActivityId; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public Double getNumericValue() { return numericValue; }
    public void setNumericValue(Double numericValue) { this.numericValue = numericValue; }

    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
