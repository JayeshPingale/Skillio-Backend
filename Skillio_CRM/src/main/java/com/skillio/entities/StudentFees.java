package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feesId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalFees; // Total course fees

    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO; // Amount paid so far

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceAmount; // Remaining amount

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO; // Discount applied

    private String discountReason; // Why discount was given

    private String paymentStatus; // PENDING, PARTIAL, FULL

    private LocalDate dueDate; // Last date to pay

    private LocalDate lastPaymentDate; // When last payment was made

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        
        calculateBalance();
        updatePaymentStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateBalance();
        updatePaymentStatus();
    }

    // ✅ CORRECTED: Balance calculation WITHOUT discount
    public void calculateBalance() {
        if (this.totalFees != null && this.paidAmount != null) {
            // ✅ Simple calculation: totalFees - paidAmount (NO DISCOUNT!)
            this.balanceAmount = this.totalFees.subtract(this.paidAmount);
        }
    }

    // ✅ CORRECTED: Payment status based on balance
    public void updatePaymentStatus() {
        if (this.balanceAmount != null) {
            if (this.balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                this.paymentStatus = "FULL";
            } else if (this.paidAmount != null && this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.paymentStatus = "PARTIAL";
            } else {
                this.paymentStatus = "PENDING";
            }
        }
    }
}
