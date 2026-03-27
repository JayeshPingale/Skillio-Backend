package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long installmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fees_id", nullable = false)
    private StudentFees studentFees;
    
    private Integer installmentNumber; // 1, 2, 3, etc.
    private LocalDate dueDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    private String status; // PENDING, PAID, OVERDUE
    private LocalDate paidDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // Which payment satisfied this installment
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
