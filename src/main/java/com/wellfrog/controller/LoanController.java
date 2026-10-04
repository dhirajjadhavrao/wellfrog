package com.wellfrog.controller;

import com.wellfrog.model.Loan;
import com.wellfrog.repository.LoanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanRepository loanRepository;

    public LoanController(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getLoans(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Loan> loans = loanRepository.findByUserIdOrderByDueDayAsc(userId);

        // Check if month rolled over for items that are not permanently CLOSED
        String currentMonth = YearMonth.now().toString();
        boolean changed = false;
        for (Loan loan : loans) {
            if ("PAID".equalsIgnoreCase(loan.getStatus()) && loan.getLastPaidMonth() != null && !loan.getLastPaidMonth().equals(currentMonth)) {
                if (loan.getRemainingTenureMonths() > 0 && loan.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                    loan.setStatus("PENDING");
                    changed = true;
                }
            }
        }
        if (changed) {
            loanRepository.saveAll(loans);
        }
        return ResponseEntity.ok(loans);
    }

    @PostMapping
    public ResponseEntity<Loan> addLoan(Authentication authentication, @RequestBody Loan loan) {
        Long userId = (Long) authentication.getPrincipal();
        loan.setUserId(userId);
        if (loan.getLoanType() == null || loan.getLoanType().isBlank()) {
            loan.setLoanType("LOAN");
        }
        if (loan.getStatus() == null || loan.getStatus().isBlank()) {
            loan.setStatus("PENDING");
        }
        if (loan.getRemainingAmount() == null || loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setRemainingAmount(loan.getLoanAmount() != null ? loan.getLoanAmount() : BigDecimal.ZERO);
        }
        if (loan.getRemainingTenureMonths() == null || loan.getRemainingTenureMonths() <= 0) {
            loan.setRemainingTenureMonths(loan.getTenureMonths() != null ? loan.getTenureMonths() : 0);
        }
        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLoan(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Loan updated) {
        Long userId = (Long) authentication.getPrincipal();
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan == null || !loan.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Loan not found"));
        }

        if (updated.getLoanType() != null) loan.setLoanType(updated.getLoanType());
        if (updated.getBankName() != null) loan.setBankName(updated.getBankName());
        if (updated.getLoanName() != null) loan.setLoanName(updated.getLoanName());
        if (updated.getLoanAmount() != null) loan.setLoanAmount(updated.getLoanAmount());
        if (updated.getRemainingAmount() != null) loan.setRemainingAmount(updated.getRemainingAmount());
        if (updated.getEmiAmount() != null) loan.setEmiAmount(updated.getEmiAmount());
        if (updated.getTenureMonths() != null) loan.setTenureMonths(updated.getTenureMonths());
        if (updated.getRemainingTenureMonths() != null) loan.setRemainingTenureMonths(updated.getRemainingTenureMonths());
        if (updated.getInterestRate() != null) loan.setInterestRate(updated.getInterestRate());
        if (updated.getDueDay() != null) loan.setDueDay(updated.getDueDay());
        if (updated.getStatus() != null) loan.setStatus(updated.getStatus());
        if (updated.getNote() != null) loan.setNote(updated.getNote());

        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLoanStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Long userId = (Long) authentication.getPrincipal();
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan == null || !loan.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Loan not found"));
        }

        String targetStatus = payload.get("status");
        if (targetStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        targetStatus = targetStatus.toUpperCase();

        String oldStatus = loan.getStatus();

        if ("PAID".equals(targetStatus)) {
            loan.setStatus("PAID");
            loan.setLastPaidDate(LocalDate.now());
            loan.setLastPaidMonth(YearMonth.now().toString());

            // If it wasn't already PAID, deduct EMI and decrement remaining tenure
            if (!"PAID".equalsIgnoreCase(oldStatus) && !"CLOSED".equalsIgnoreCase(oldStatus)) {
                // Deduct remaining tenure by 1
                int currentTenure = loan.getRemainingTenureMonths();
                if (currentTenure > 0) {
                    loan.setRemainingTenureMonths(currentTenure - 1);
                }

                // Deduct EMI amount from remainingAmount
                BigDecimal emi = loan.getEmiAmount() != null ? loan.getEmiAmount() : BigDecimal.ZERO;
                BigDecimal remaining = loan.getRemainingAmount() != null ? loan.getRemainingAmount() : BigDecimal.ZERO;
                BigDecimal newRemaining = remaining.subtract(emi);
                if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                    newRemaining = BigDecimal.ZERO;
                }
                loan.setRemainingAmount(newRemaining);

                // If remaining tenure is 0 or remaining balance is 0, mark as CLOSED
                if (loan.getRemainingTenureMonths() <= 0 && loan.getTenureMonths() > 0) {
                    loan.setStatus("CLOSED");
                }
            }
        } else if ("PENDING".equals(targetStatus)) {
            // If reverting back from PAID or CLOSED to PENDING
            if ("PAID".equalsIgnoreCase(oldStatus) || "CLOSED".equalsIgnoreCase(oldStatus)) {
                // Add back 1 month tenure (capped at tenureMonths)
                int currentTenure = loan.getRemainingTenureMonths();
                int totalTenure = loan.getTenureMonths();
                if (totalTenure > 0 && currentTenure < totalTenure) {
                    loan.setRemainingTenureMonths(currentTenure + 1);
                }

                // Add back EMI amount to remainingAmount (capped at loanAmount)
                BigDecimal emi = loan.getEmiAmount() != null ? loan.getEmiAmount() : BigDecimal.ZERO;
                BigDecimal remaining = loan.getRemainingAmount() != null ? loan.getRemainingAmount() : BigDecimal.ZERO;
                BigDecimal totalLoan = loan.getLoanAmount() != null ? loan.getLoanAmount() : BigDecimal.ZERO;
                BigDecimal restored = remaining.add(emi);
                if (totalLoan.compareTo(BigDecimal.ZERO) > 0 && restored.compareTo(totalLoan) > 0) {
                    restored = totalLoan;
                }
                loan.setRemainingAmount(restored);
                loan.setLastPaidMonth(null);
            }
            loan.setStatus("PENDING");
        } else if ("FAILED".equals(targetStatus)) {
            loan.setStatus("FAILED");
        } else if ("CLOSED".equals(targetStatus)) {
            loan.setStatus("CLOSED");
            loan.setRemainingTenureMonths(0);
            loan.setRemainingAmount(BigDecimal.ZERO);
        }

        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLoan(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan != null && loan.getUserId().equals(userId)) {
            loanRepository.delete(loan);
            return ResponseEntity.ok(Map.of("message", "Loan deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Loan not found"));
    }
}
