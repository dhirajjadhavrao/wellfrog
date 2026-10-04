package com.wellfrog.controller;

import com.wellfrog.model.*;
import com.wellfrog.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ExpenseRepository expenseRepository;
    private final LoanRepository loanRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkSessionRepository workSessionRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ActivityRepository activityRepository;

    public DashboardController(ExpenseRepository expenseRepository,
                               LoanRepository loanRepository,
                               WorkoutRepository workoutRepository,
                               WorkSessionRepository workSessionRepository,
                               JobApplicationRepository jobApplicationRepository,
                               ActivityRepository activityRepository) {
        this.expenseRepository = expenseRepository;
        this.loanRepository = loanRepository;
        this.workoutRepository = workoutRepository;
        this.workSessionRepository = workSessionRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.activityRepository = activityRepository;
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyDashboard(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Long userId = (Long) authentication.getPrincipal();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 1. Finance - Today's Expenses
        List<Expense> dailyExpenses = expenseRepository.findByUserIdAndExpenseDate(userId, targetDate);
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal onlineSpent = BigDecimal.ZERO;
        BigDecimal offlineSpent = BigDecimal.ZERO;

        for (Expense e : dailyExpenses) {
            totalSpent = totalSpent.add(e.getAmount());
            if ("ONLINE".equalsIgnoreCase(e.getMode())) {
                onlineSpent = onlineSpent.add(e.getAmount());
            } else {
                offlineSpent = offlineSpent.add(e.getAmount());
            }
        }

        // 2. Finance - Loans & EMIs Summary
        List<Loan> allLoans = loanRepository.findByUserIdOrderByDueDayAsc(userId);
        BigDecimal totalEmi = BigDecimal.ZERO;
        int paidCount = 0;
        int failedCount = 0;
        int pendingCount = 0;

        for (Loan l : allLoans) {
            totalEmi = totalEmi.add(l.getEmiAmount());
            if ("PAID".equalsIgnoreCase(l.getStatus())) paidCount++;
            else if ("FAILED".equalsIgnoreCase(l.getStatus())) failedCount++;
            else pendingCount++;
        }

        // 3. Workouts
        List<Workout> dailyWorkouts = workoutRepository.findByUserIdAndWorkoutDate(userId, targetDate);
        int totalWorkoutMins = dailyWorkouts.stream().mapToInt(Workout::getDurationMinutes).sum();

        // 4. Office Work
        WorkSession workSession = workSessionRepository.findByUserIdAndSessionDate(userId, targetDate).orElse(null);

        // 5. Job Applications
        List<JobApplication> jobs = jobApplicationRepository.findByUserIdOrderByAppliedDateDesc(userId);
        long appliedCount = jobs.stream().filter(j -> "APPLIED".equalsIgnoreCase(j.getStatus())).count();
        long interviewCount = jobs.stream().filter(j -> "TECH_INTERVIEW".equalsIgnoreCase(j.getStatus()) || "HR_CALL".equalsIgnoreCase(j.getStatus())).count();
        long offerCount = jobs.stream().filter(j -> "OFFER".equalsIgnoreCase(j.getStatus())).count();

        // 6. Root Activities
        List<Activity> rootActivities = activityRepository.findByUserIdAndParentIdIsNull(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("date", targetDate);
        
        response.put("finance", Map.of(
                "totalSpent", totalSpent,
                "onlineSpent", onlineSpent,
                "offlineSpent", offlineSpent,
                "expenses", dailyExpenses
        ));

        response.put("loans", Map.of(
                "totalEmiLiability", totalEmi,
                "paidCount", paidCount,
                "failedCount", failedCount,
                "pendingCount", pendingCount,
                "loanList", allLoans
        ));

        response.put("workouts", Map.of(
                "completed", !dailyWorkouts.isEmpty(),
                "totalMinutes", totalWorkoutMins,
                "workoutList", dailyWorkouts
        ));

        response.put("work", Map.of(
                "hoursWorked", workSession != null ? workSession.getHoursWorked() : 0.0,
                "tasks", workSession != null && workSession.getTasks() != null ? workSession.getTasks() : "",
                "notes", workSession != null && workSession.getNotes() != null ? workSession.getNotes() : ""
        ));

        response.put("jobs", Map.of(
                "totalTracked", jobs.size(),
                "appliedCount", appliedCount,
                "interviewCount", interviewCount,
                "offerCount", offerCount,
                "recentJobs", jobs
        ));

        response.put("activities", rootActivities);

        return ResponseEntity.ok(response);
    }
}
