package com.skillio.services;

import com.skillio.dto.CreatePaymentInstallmentRequest;
import com.skillio.dto.UpdatePaymentInstallmentRequest;
import com.skillio.dto.PaymentInstallmentResponse;
import com.skillio.entities.PaymentInstallment;

import java.util.List;

public interface PaymentInstallmentService {
    
    // Create
    List<PaymentInstallmentResponse> createInstallmentPlan(CreatePaymentInstallmentRequest request, Long loggedInUserId);
    
    // Read
    PaymentInstallmentResponse getInstallmentById(Long installmentId);
    List<PaymentInstallmentResponse> getAllInstallments();
    List<PaymentInstallmentResponse> getInstallmentsByStudentFeesId(Long feesId);
    List<PaymentInstallmentResponse> getInstallmentsByStatus(String status);
    List<PaymentInstallmentResponse> getOverdueInstallments();
    List<PaymentInstallmentResponse> getPendingInstallments();
    
    // Update
    PaymentInstallmentResponse updateInstallment(Long installmentId, UpdatePaymentInstallmentRequest request, Long loggedInUserId);
    PaymentInstallmentResponse markAsPaid(Long installmentId, Long paymentId, Long loggedInUserId);
    PaymentInstallmentResponse markAsOverdue(Long installmentId, Long loggedInUserId);
    
    // Delete
    void deleteInstallment(Long installmentId, Long loggedInUserId);
    void deleteAllInstallmentsForFees(Long feesId, Long loggedInUserId);
    
    // Helper
    PaymentInstallment getInstallmentEntityById(Long installmentId);
}
