package com.wellfrog.service;

import com.wellfrog.config.JwtUtil;
import com.wellfrog.model.*;
import com.wellfrog.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final LoanRepository loanRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       ActivityRepository activityRepository,
                       LoanRepository loanRepository,
                       JobApplicationRepository jobApplicationRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.loanRepository = loanRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, Object> processUserLogin(String email, String name, String pictureUrl, String googleId) {
        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = false;

        if (user == null) {
            user = new User(email, name, pictureUrl, googleId);
            user = userRepository.save(user);
            isNewUser = true;
            seedDefaultActivitiesForUser(user.getId());
        } else {
            // Update latest name and picture if changed
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "pictureUrl", user.getPictureUrl() != null ? user.getPictureUrl() : ""
                ),
                "isNewUser", isNewUser
        );
    }

    private void seedDefaultActivitiesForUser(Long userId) {
        // 1. Finance Activity & Sub-activities
        Activity finance = activityRepository.save(new Activity(userId, null, "Finance", "FINANCE", "CURRENCY", "#2E7D32", "Wallet"));
        activityRepository.save(new Activity(userId, finance.getId(), "Food & Dining", "FINANCE", "CURRENCY", "#4CAF50", "Utensils"));
        activityRepository.save(new Activity(userId, finance.getId(), "Groceries", "FINANCE", "CURRENCY", "#66BB6A", "ShoppingCart"));
        activityRepository.save(new Activity(userId, finance.getId(), "Travel & Fuel", "FINANCE", "CURRENCY", "#81C784", "Car"));
        activityRepository.save(new Activity(userId, finance.getId(), "Shopping", "FINANCE", "CURRENCY", "#A5D6A7", "ShoppingBag"));
        activityRepository.save(new Activity(userId, finance.getId(), "Bills & Utilities", "FINANCE", "CURRENCY", "#C8E6C9", "Receipt"));

        // 2. Workout Activity & Sub-activities
        Activity workout = activityRepository.save(new Activity(userId, null, "Workout", "WORKOUT", "MINUTES", "#D84315", "Dumbbell"));
        activityRepository.save(new Activity(userId, workout.getId(), "Gym / Strength", "WORKOUT", "MINUTES", "#FF5722", "Activity"));
        activityRepository.save(new Activity(userId, workout.getId(), "Cardio / Running", "WORKOUT", "MINUTES", "#FF7043", "Flame"));
        activityRepository.save(new Activity(userId, workout.getId(), "Walking / Steps", "WORKOUT", "MINUTES", "#FF8A65", "Footprints"));
        activityRepository.save(new Activity(userId, workout.getId(), "Yoga & Stretch", "WORKOUT", "MINUTES", "#FFAB91", "HeartPulse"));

        // 3. Screen Time Activity & Sub-activities
        Activity screenTime = activityRepository.save(new Activity(userId, null, "Screen Time", "SCREEN_TIME", "MINUTES", "#1565C0", "Smartphone"));
        activityRepository.save(new Activity(userId, screenTime.getId(), "Social Media", "SCREEN_TIME", "MINUTES", "#1976D2", "Share2"));
        activityRepository.save(new Activity(userId, screenTime.getId(), "Entertainment & Video", "SCREEN_TIME", "MINUTES", "#2196F3", "PlayCircle"));
        activityRepository.save(new Activity(userId, screenTime.getId(), "Productivity Apps", "SCREEN_TIME", "MINUTES", "#42A5F5", "CheckCircle2"));
        activityRepository.save(new Activity(userId, screenTime.getId(), "Mobile Gaming", "SCREEN_TIME", "MINUTES", "#64B5F6", "Gamepad2"));

        // 4. Work Time Activity & Sub-activities
        Activity workTime = activityRepository.save(new Activity(userId, null, "Office Work", "WORK_TIME", "MINUTES", "#6A1B9A", "Briefcase"));
        activityRepository.save(new Activity(userId, workTime.getId(), "Deep Work / Coding", "WORK_TIME", "MINUTES", "#8E24AA", "Code"));
        activityRepository.save(new Activity(userId, workTime.getId(), "Meetings & Calls", "WORK_TIME", "MINUTES", "#AB47BC", "Users"));
        activityRepository.save(new Activity(userId, workTime.getId(), "Admin & Emails", "WORK_TIME", "MINUTES", "#BA68C8", "Mail"));

        // Seed Sample Loan / EMI for demonstration
        loanRepository.save(new Loan(userId, "Personal / Vehicle Loan", new BigDecimal("15000.00"), 10, "PENDING", "Monthly auto-debit on 10th"));

        // Seed Sample Naukri / Job Application
        jobApplicationRepository.save(new JobApplication(
                userId, "Mastercard", "Senior Java Engineer", "Naukri", "APPLIED",
                LocalDate.now().minusDays(2), "Application viewed by recruiter", "Applied via Naukri profile"
        ));
    }
}
