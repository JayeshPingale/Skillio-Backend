package com.skillio.services;

import com.skillio.dto.CreatePaymentRequest;
import com.skillio.dto.PaymentResponse;
import com.skillio.entities.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    
    // Create
    PaymentResponse createPayment(CreatePaymentRequest request, Long loggedInUserId);
    
    // Read
    PaymentResponse getPaymentById(Long paymentId);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByStudent(Long studentId);
    List<PaymentResponse> getPaymentsByStudentFees(Long feesId);
    List<PaymentResponse> getPaymentsByReceivedBy(Long userId);
    List<PaymentResponse> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate);
    List<PaymentResponse> getPaymentsByStatus(String status);
    
    // Analytics
    BigDecimal getTotalPaidAmountByStudent(Long studentId);
    Long getTotalPaymentsCountByReceivedBy(Long userId);
    
    // Update
    PaymentResponse updatePaymentStatus(Long paymentId, String status, Long loggedInUserId);
    
    // Delete
    void deletePayment(Long paymentId, Long loggedInUserId);
    
    // Helper
    Payment getPaymentEntityById(Long paymentId);
}
