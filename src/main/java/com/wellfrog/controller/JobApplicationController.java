package com.wellfrog.controller;

import com.wellfrog.model.JobApplication;
import com.wellfrog.repository.JobApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

    private final JobApplicationRepository jobApplicationRepository;

    public JobApplicationController(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getJobApplications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(jobApplicationRepository.findByUserIdOrderByAppliedDateDesc(userId));
    }

    @PostMapping
    public ResponseEntity<JobApplication> addJobApplication(
            Authentication authentication,
            @RequestBody JobApplication jobApplication) {
        Long userId = (Long) authentication.getPrincipal();
        jobApplication.setUserId(userId);
        if (jobApplication.getAppliedDate() == null) {
            jobApplication.setAppliedDate(LocalDate.now());
        }
        if (jobApplication.getStatus() == null || jobApplication.getStatus().isBlank()) {
            jobApplication.setStatus("APPLIED");
        }
        if (jobApplication.getPlatform() == null || jobApplication.getPlatform().isBlank()) {
            jobApplication.setPlatform("Naukri");
        }
        return ResponseEntity.ok(jobApplicationRepository.save(jobApplication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobApplication(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody JobApplication payload) {
        Long userId = (Long) authentication.getPrincipal();
        JobApplication existing = jobApplicationRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
            if (payload.getLastUpdate() != null) existing.setLastUpdate(payload.getLastUpdate());
            if (payload.getNotes() != null) existing.setNotes(payload.getNotes());
            if (payload.getRole() != null) existing.setRole(payload.getRole());
            if (payload.getCompany() != null) existing.setCompany(payload.getCompany());
            return ResponseEntity.ok(jobApplicationRepository.save(existing));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Job application not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJobApplication(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        JobApplication existing = jobApplicationRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            jobApplicationRepository.delete(existing);
            return ResponseEntity.ok(Map.of("message", "Job application deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Job application not found"));
    }
}
