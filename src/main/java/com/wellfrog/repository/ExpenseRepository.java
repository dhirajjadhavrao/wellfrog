package com.wellfrog.repository;

import com.wellfrog.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdAndExpenseDate(Long userId, LocalDate expenseDate);
    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId, LocalDate start, LocalDate end);
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);
}
