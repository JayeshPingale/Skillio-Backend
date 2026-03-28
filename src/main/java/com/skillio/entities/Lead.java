// Lead.java
package com.skillio.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leadId;

    private String fullName;
    private String contactNumber;
    private String email;
    private String courseInterested;
    private String collegeName;
    private String qualification;
    private String experience;
    private String status;
    private String interestLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private LeadSource leadSource;

    private LocalDateTime createdDate;
    private LocalDate lastContactDate;
    private LocalDate conversionDate;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ✅ FIXED: Make nullable, no EAGER fetch (causes N+1 queries)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_executive_id", nullable = true) // ✅ Allow NULL
    @NotFound(action = NotFoundAction.IGNORE)
    private User salesExecutive;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student convertedStudent;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "NEW";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
