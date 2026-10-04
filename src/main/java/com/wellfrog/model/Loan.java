package com.wellfrog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String loanName; // e.g. HDFC Home Loan, Personal Loan, Car Loan, Card EMI

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false)
    private Integer dueDay; // Day of the month: 1 - 31

    @Column(nullable = false)
    private String status; // PAID, PENDING, FAILED

    private LocalDate lastPaidDate;

    private String note;

    public Loan() {}

    public Loan(Long userId, String loanName, BigDecimal emiAmount, Integer dueDay, String status, String note) {
        this.userId = userId;
        this.loanName = loanName;
        this.emiAmount = emiAmount;
        this.dueDay = dueDay;
        this.status = status != null ? status : "PENDING";
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLoanName() { return loanName; }
    public void setLoanName(String loanName) { this.loanName = loanName; }

    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }

    public Integer getDueDay() { return dueDay; }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getLastPaidDate() { return lastPaidDate; }
    public void setLastPaidDate(LocalDate lastPaidDate) { this.lastPaidDate = lastPaidDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
