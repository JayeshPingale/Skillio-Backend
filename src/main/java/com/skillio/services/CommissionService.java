package com.skillio.services;

import java.math.BigDecimal;
import java.util.List;

import com.skillio.dto.ApproveCommissionRequest;
import com.skillio.dto.CommissionResponse;
import com.skillio.dto.CreateCommissionRequest;
import com.skillio.dto.EnrolledStudentCommissionView;
import com.skillio.dto.UpdateCommissionRequest;
import com.skillio.entities.Commission;

public interface CommissionService {
    
    // Create
    CommissionResponse createCommission(CreateCommissionRequest request, Long loggedInUserId);
    
    // Read
    CommissionResponse getCommissionById(Long commissionId);
    List<CommissionResponse> getAllCommissions();
    List<CommissionResponse> getCommissionsBySalesExecutive(Long salesExecutiveId);
    List<CommissionResponse> getCommissionsBySalesExecutiveAndStatus(Long salesExecutiveId, String status);
    List<CommissionResponse> getCommissionsByStatus(String status);
    List<CommissionResponse> getCommissionsByEnrollment(Long enrollmentId);
    
    // Analytics
    BigDecimal getTotalEligibleAmountBySalesExecutive(Long salesExecutiveId);
    BigDecimal getTotalPaidCommissionBySalesExecutive(Long salesExecutiveId);
    Long getCommissionCountBySalesExecutiveAndStatus(Long salesExecutiveId, String status);
    
    // Update
    CommissionResponse updateCommission(Long commissionId, UpdateCommissionRequest request, Long loggedInUserId);
    
    // ✅ NEW METHOD - Approve Commission
    CommissionResponse approveCommission(ApproveCommissionRequest request, Long loggedInUserId);
    
    CommissionResponse markAsEligible(Long commissionId, Long loggedInUserId);
    CommissionResponse markAsPaid(Long commissionId, Long loggedInUserId);
    
    // Delete
    void deleteCommission(Long commissionId, Long loggedInUserId);
    
    CommissionResponse approveOrRejectCommission(ApproveCommissionRequest request, Long adminId);
    // Helper
    Commission getCommissionEntityById(Long commissionId);

	CommissionResponse requestCommission(CreateCommissionRequest request, Long salesExecutiveId);
	
	
	// CommissionService.java interface mein yeh 3 add karo:



	// Sales Exec ke enrolled students
	List<EnrolledStudentCommissionView> getEnrolledStudentsForCommission(Long salesExecutiveId);



}
