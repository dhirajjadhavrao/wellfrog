package com.wellfrog.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String company; // e.g. Mastercard, Google, Startup

    @Column(nullable = false)
    private String role; // e.g. Senior Java Engineer, Tech Lead

    @Column(nullable = false)
    private String platform; // Naukri, LinkedIn, Referral, Direct

    @Column(nullable = false)
    private String status; // APPLIED, SHORTLISTED, HR_CALL, TECH_INTERVIEW, OFFER, REJECTED

    @Column(nullable = false)
    private LocalDate appliedDate;

    private String lastUpdate; // e.g. HR call on Wednesday 3pm

    @Column(columnDefinition = "TEXT")
    private String notes;

    public JobApplication() {}

    public JobApplication(Long userId, String company, String role, String platform, String status, LocalDate appliedDate, String lastUpdate, String notes) {
        this.userId = userId;
        this.company = company;
        this.role = role;
        this.platform = platform;
        this.status = status;
        this.appliedDate = appliedDate;
        this.lastUpdate = lastUpdate;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
