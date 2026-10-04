package com.wellfrog.controller;

import com.wellfrog.model.Loan;
import com.wellfrog.repository.LoanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        return ResponseEntity.ok(loanRepository.findByUserIdOrderByDueDayAsc(userId));
    }

    @PostMapping
    public ResponseEntity<Loan> addLoan(Authentication authentication, @RequestBody Loan loan) {
        Long userId = (Long) authentication.getPrincipal();
        loan.setUserId(userId);
        if (loan.getStatus() == null) {
            loan.setStatus("PENDING");
        }
        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLoanStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Long userId = (Long) authentication.getPrincipal();
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan != null && loan.getUserId().equals(userId)) {
            String newStatus = payload.get("status");
            if (newStatus != null) {
                loan.setStatus(newStatus.toUpperCase());
                if ("PAID".equalsIgnoreCase(newStatus)) {
                    loan.setLastPaidDate(LocalDate.now());
                }
            }
            return ResponseEntity.ok(loanRepository.save(loan));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Loan not found"));
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
