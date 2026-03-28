package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long commissionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "enrollment_id", nullable = false)
	private Enrollment enrollment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sales_executive_id", nullable = false)
	private User salesExecutive;

	@Column(precision = 10, scale = 2)
	private BigDecimal totalCourseFees; // ✅ After discount

	// ✅ OPTIONAL: Add these for better tracking
	@Column(precision = 10, scale = 2)
	private BigDecimal originalCourseFees; // Before discount

	@Column(precision = 10, scale = 2)
	private BigDecimal discountAmount; // Discount applied

	@Column(precision = 5, scale = 2)
	private BigDecimal commissionPercentage = new BigDecimal("10.00");

	@Column(precision = 10, scale = 2)
	private BigDecimal eligibleAmount;

	private String status;
	// Values: PENDING_APPROVAL, APPROVED, REJECTED, PAID

	// ✅ ADD: New field — who requested + rejection reason
	private String requestedRemarks; // Sales Exec's request note
	private String adminComments; // Admin's approval/rejection comment

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		if (this.status == null) {
			this.status = "PENDING_APPROVAL"; // ✅ Changed from PENDING
		}
		calculateEligibleAmount();
	}

	private String eligibilityCondition;
	private LocalDate eligibilityDate;
	private LocalDate paidDate;

	@Column(columnDefinition = "TEXT")
	private String remarks;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//        if (this.status == null) {
//            this.status = "PENDING";
//        }
//        calculateEligibleAmount();
//    }
//    
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// Helper method to calculate eligible amount
	public void calculateEligibleAmount() {
		if (this.totalCourseFees != null && this.commissionPercentage != null) {
			this.eligibleAmount = this.totalCourseFees.multiply(this.commissionPercentage).divide(new BigDecimal("100"),
					2, java.math.RoundingMode.HALF_UP);
		}
	}
}
