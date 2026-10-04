package com.wellfrog.controller;

import com.wellfrog.model.Expense;
import com.wellfrog.repository.ExpenseRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;

    public ExpenseController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) authentication.getPrincipal();
        if (date != null) {
            return ResponseEntity.ok(expenseRepository.findByUserIdAndExpenseDate(userId, date));
        }
        return ResponseEntity.ok(expenseRepository.findByUserIdOrderByExpenseDateDesc(userId));
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(Authentication authentication, @RequestBody Expense expense) {
        Long userId = (Long) authentication.getPrincipal();
        expense.setUserId(userId);
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        if (expense.getMode() == null || expense.getMode().isBlank()) {
            expense.setMode("ONLINE");
        }
        return ResponseEntity.ok(expenseRepository.save(expense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null && expense.getUserId().equals(userId)) {
            expenseRepository.delete(expense);
            return ResponseEntity.ok(Map.of("message", "Expense deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Expense not found"));
    }
}
