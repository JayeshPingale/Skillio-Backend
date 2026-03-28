package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commissionPaymentId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_id", nullable = false, unique = true)
    private Commission commission;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amountPaid;
    
    private LocalDate paymentDate;
    private String paymentMode; // BANK_TRANSFER, CASH, CHEQUE, etc.
    private String transactionId; // Bank/Payment gateway reference
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy; // Admin who paid the commission
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
