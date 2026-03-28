package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;
    
    private String invoiceNumber; // INV_2025_001, etc.
    private LocalDateTime generatedDate;
    private String pdfPath; // Path where PDF is stored
    
    private Boolean sentToEmail = false;
    private Boolean sentToWhatsApp = false;
    private LocalDateTime sentDate;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.generatedDate == null) {
            this.generatedDate = LocalDateTime.now();
        }
    }
}
