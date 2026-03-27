package com.skillio.services;

import com.skillio.dto.CreateCommissionPaymentRequest;
import com.skillio.dto.UpdateCommissionPaymentRequest;
import com.skillio.dto.CommissionPaymentResponse;
import com.skillio.entities.CommissionPayment;

import java.util.List;

public interface CommissionPaymentService {
    
    // Create
    CommissionPaymentResponse payCommission(CreateCommissionPaymentRequest request, Long loggedInUserId);
    
    // Read
    CommissionPaymentResponse getCommissionPaymentById(Long commissionPaymentId);
    List<CommissionPaymentResponse> getAllCommissionPayments();
    CommissionPaymentResponse getCommissionPaymentByCommissionId(Long commissionId);
    List<CommissionPaymentResponse> getCommissionPaymentsPaidBy(Long paidByUserId);
    
    // Update
    CommissionPaymentResponse updateCommissionPayment(Long commissionPaymentId, UpdateCommissionPaymentRequest request, Long loggedInUserId);
    
    // Delete
    void deleteCommissionPayment(Long commissionPaymentId, Long loggedInUserId);
    
    // Helper
    CommissionPayment getCommissionPaymentEntityById(Long commissionPaymentId);
}
