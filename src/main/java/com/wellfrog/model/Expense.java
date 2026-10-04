package com.wellfrog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String mode; // ONLINE (UPI/Card/Netbanking), OFFLINE (Cash)

    @Column(nullable = false)
    private String category; // Food & Dining, Groceries, Travel, Shopping, Bills, Entertainment, Misc

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String note;

    public Expense() {}

    public Expense(Long userId, BigDecimal amount, String mode, String category, LocalDate expenseDate, String note) {
        this.userId = userId;
        this.amount = amount;
        this.mode = mode;
        this.category = category;
        this.expenseDate = expenseDate;
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
