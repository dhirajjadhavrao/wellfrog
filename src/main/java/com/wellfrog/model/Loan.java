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

    @Column(name = "loan_type")
    private String loanType = "LOAN"; // "LOAN" or "CREDIT_CARD"

    private String bankName; // e.g. HDFC Bank, SBI, ICICI, Axis, Amex

    @Column(nullable = false)
    private String loanName; // e.g. Home Loan, Personal Loan, Millennia Credit Card

    @Column(precision = 14, scale = 2)
    private BigDecimal loanAmount = BigDecimal.ZERO; // Total Principal or Card Limit / Outstanding

    @Column(precision = 14, scale = 2)
    private BigDecimal remainingAmount = BigDecimal.ZERO; // Current remaining balance

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal emiAmount = BigDecimal.ZERO; // Monthly EMI or Card bill amount

    private Integer tenureMonths = 0; // Total tenure in months

    private Integer remainingTenureMonths = 0; // Remaining tenure in months

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO; // Rate of interest %

    @Column(nullable = false)
    private Integer dueDay = 5; // Day of the month: 1 - 31

    @Column(nullable = false)
    private String status = "PENDING"; // PAID, PENDING, FAILED, CLOSED

    private LocalDate lastPaidDate;

    private String lastPaidMonth; // Tracks payment month e.g. "2026-10"

    private String note;

    public Loan() {}

    public Loan(Long userId, String loanName, BigDecimal emiAmount, Integer dueDay, String status, String note) {
        this.userId = userId;
        this.loanName = loanName;
        this.emiAmount = emiAmount != null ? emiAmount : BigDecimal.ZERO;
        this.dueDay = dueDay != null ? dueDay : 5;
        this.status = status != null ? status : "PENDING";
        this.note = note;
        this.loanType = "LOAN";
        this.bankName = "HDFC Bank";
        this.tenureMonths = 24;
        this.remainingTenureMonths = 18;
        this.loanAmount = this.emiAmount.multiply(BigDecimal.valueOf(24));
        this.remainingAmount = this.emiAmount.multiply(BigDecimal.valueOf(18));
        this.interestRate = new BigDecimal("8.50");
    }

    public Loan(Long userId, String loanType, String bankName, String loanName, BigDecimal loanAmount,
                BigDecimal remainingAmount, BigDecimal emiAmount, Integer tenureMonths,
                Integer remainingTenureMonths, BigDecimal interestRate, Integer dueDay, String status, String note) {
        this.userId = userId;
        this.loanType = loanType != null ? loanType : "LOAN";
        this.bankName = bankName;
        this.loanName = loanName;
        this.loanAmount = loanAmount != null ? loanAmount : BigDecimal.ZERO;
        this.remainingAmount = remainingAmount != null ? remainingAmount : this.loanAmount;
        this.emiAmount = emiAmount != null ? emiAmount : BigDecimal.ZERO;
        this.tenureMonths = tenureMonths != null ? tenureMonths : 0;
        this.remainingTenureMonths = remainingTenureMonths != null ? remainingTenureMonths : this.tenureMonths;
        this.interestRate = interestRate != null ? interestRate : BigDecimal.ZERO;
        this.dueDay = dueDay != null ? dueDay : 5;
        this.status = status != null ? status : "PENDING";
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLoanType() {
        return loanType != null ? loanType : "LOAN";
    }
    public void setLoanType(String loanType) {
        this.loanType = loanType != null ? loanType : "LOAN";
    }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getLoanName() { return loanName; }
    public void setLoanName(String loanName) { this.loanName = loanName; }

    public BigDecimal getLoanAmount() {
        return loanAmount != null ? loanAmount : BigDecimal.ZERO;
    }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

    public BigDecimal getRemainingAmount() {
        return remainingAmount != null ? remainingAmount : getLoanAmount();
    }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public BigDecimal getEmiAmount() {
        return emiAmount != null ? emiAmount : BigDecimal.ZERO;
    }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }

    public Integer getTenureMonths() {
        return tenureMonths != null ? tenureMonths : 0;
    }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public Integer getRemainingTenureMonths() {
        return remainingTenureMonths != null ? remainingTenureMonths : getTenureMonths();
    }
    public void setRemainingTenureMonths(Integer remainingTenureMonths) { this.remainingTenureMonths = remainingTenureMonths; }

    public BigDecimal getInterestRate() {
        return interestRate != null ? interestRate : BigDecimal.ZERO;
    }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getDueDay() {
        return dueDay != null ? dueDay : 5;
    }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }

    public String getStatus() {
        return status != null ? status : "PENDING";
    }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getLastPaidDate() { return lastPaidDate; }
    public void setLastPaidDate(LocalDate lastPaidDate) { this.lastPaidDate = lastPaidDate; }

    public String getLastPaidMonth() { return lastPaidMonth; }
    public void setLastPaidMonth(String lastPaidMonth) { this.lastPaidMonth = lastPaidMonth; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
