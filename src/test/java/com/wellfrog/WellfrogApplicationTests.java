package com.wellfrog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WellfrogApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete E2E Flow: Auth -> 5 Hubs (Spend, Loans, Workouts, Work, Naukri) -> Custom Activities")
    void testCompleteE2EFlow() throws Exception {
        // 1. Dev Login
        Map<String, String> loginRequest = Map.of(
                "email", "test.user@wellfrog.app",
                "name", "Wellfrog Test User"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();
        assertThat(token).isNotBlank();

        String authHeader = "Bearer " + token;

        // Verify Me endpoint
        mockMvc.perform(get("/api/auth/me").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test.user@wellfrog.app"));

        // 2. Hub 1: Finance - Daily Spend (Online & Offline)
        LocalDate today = LocalDate.now();

        Map<String, Object> onlineExpense = Map.of(
                "amount", new BigDecimal("450.00"),
                "category", "Food & Dining",
                "mode", "ONLINE",
                "expenseDate", today.toString(),
                "note", "Swiggy lunch"
        );
        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlineExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("ONLINE"))
                .andExpect(jsonPath("$.amount").value(450.00));

        Map<String, Object> offlineExpense = Map.of(
                "amount", new BigDecimal("120.00"),
                "category", "Groceries",
                "mode", "OFFLINE",
                "expenseDate", today.toString(),
                "note", "Vegetables cash"
        );
        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offlineExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("OFFLINE"));

        // 3. Hub 2: Loans & EMIs
        Map<String, Object> homeLoan = Map.of(
                "loanName", "HDFC Home Loan",
                "totalPrincipal", new BigDecimal("3500000.00"),
                "emiAmount", new BigDecimal("32500.00"),
                "dueDay", 5,
                "status", "PENDING"
        );
        MvcResult loanResult = mockMvc.perform(post("/api/loans")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(homeLoan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode loanJson = objectMapper.readTree(loanResult.getResponse().getContentAsString());
        long loanId = loanJson.get("id").asLong();

        // Update status to PAID
        mockMvc.perform(put("/api/loans/" + loanId + "/status")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PAID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // 4. Hub 3: Workout Sessions
        Map<String, Object> workout = Map.of(
                "workoutType", "Gym / Strength",
                "durationMinutes", 60,
                "workoutDate", today.toString(),
                "notes", "Chest and triceps routine"
        );
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workout)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(60));

        // 5. Hub 4: Office Work & Tasks
        Map<String, Object> workSession = Map.of(
                "sessionDate", today.toString(),
                "hoursWorked", 8.5,
                "tasks", "[x] Release pipeline fix\n[ ] Review PR #42\n[x] Standup meeting",
                "notes", "Productive sprint planning day"
        );
        mockMvc.perform(post("/api/work")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoursWorked").value(8.5));

        // 6. Hub 5: Naukri / Job Applications
        Map<String, Object> jobApp = Map.of(
                "company", "Google",
                "role", "Senior Software Engineer",
                "platform", "Naukri",
                "status", "APPLIED",
                "notes", "Applied via referral"
        );
        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobApp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Google"))
                .andReturn();

        long jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();

        // Update Job stage to TECH_INTERVIEW
        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "TECH_INTERVIEW",
                                "notes", "Technical round scheduled for Thursday"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TECH_INTERVIEW"));

        // 7. Verify Consolidated Daily Dashboard API
        mockMvc.perform(get("/api/dashboard/daily?date=" + today)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finance.totalSpent").value(570.00))
                .andExpect(jsonPath("$.finance.onlineSpent").value(450.00))
                .andExpect(jsonPath("$.finance.offlineSpent").value(120.00))
                .andExpect(jsonPath("$.loans.paidCount").value(1))
                .andExpect(jsonPath("$.workouts.completed").value(true))
                .andExpect(jsonPath("$.workouts.totalMinutes").value(60))
                .andExpect(jsonPath("$.work.hoursWorked").value(8.5))
                .andExpect(jsonPath("$.jobs.totalTracked").value(2));

        // 8. Custom Activity & Sub-Activity CRUD
        Map<String, Object> rootActivity = Map.of(
                "name", "Guitar Practice",
                "icon", "🎸",
                "unit", "MINUTES",
                "categoryType", "CUSTOM"
        );
        MvcResult actResult = mockMvc.perform(post("/api/activities")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rootActivity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Guitar Practice"))
                .andReturn();

        long parentActId = objectMapper.readTree(actResult.getResponse().getContentAsString()).get("id").asLong();

        Map<String, Object> subActivity = Map.of(
                "name", "Fingerstyle Exercises",
                "icon", "🎼",
                "parentId", parentActId,
                "unit", "MINUTES",
                "categoryType", "SUB_ACTIVITY"
        );
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subActivity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(parentActId));

        // Delete parent activity (cascades sub-activities)
        mockMvc.perform(delete("/api/activities/" + parentActId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());
    }
}
