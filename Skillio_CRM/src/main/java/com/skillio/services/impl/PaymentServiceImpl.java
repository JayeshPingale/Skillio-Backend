package com.skillio.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.dto.CreateInvoiceRequest;
import com.skillio.dto.CreatePaymentRequest;
import com.skillio.dto.PaymentResponse;
import com.skillio.entities.Payment;
import com.skillio.entities.Student;
import com.skillio.entities.StudentFees;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.PaymentRepository;
import com.skillio.repositories.StudentFeesRepository;
import com.skillio.repositories.StudentRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.InvoiceService;
import com.skillio.services.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentFeesRepository studentFeesRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService; // ✅ Use dedicated audit service
    private final InvoiceService invoiceService;

//    @Override
//    @Transactional
//    public PaymentResponse createPayment(CreatePaymentRequest request, Long loggedInUserId) {
//        log.info("Creating new payment for fees ID: {}", request.getFeesId());
//
//        // Validate StudentFees exists
//        StudentFees studentFees = studentFeesRepository.findById(request.getFeesId())
//                .orElseThrow(() -> new ResourceNotFoundException("StudentFees not found with ID: " + request.getFeesId()));
//
//        // Validate payment amount doesn't exceed balance
//        BigDecimal balanceAmount = studentFees.getBalanceAmount();
//        if (request.getAmount().compareTo(balanceAmount) > 0) {
//            throw new IllegalArgumentException(
//                    String.format("Payment amount ₹%.2f exceeds balance amount ₹%.2f", 
//                            request.getAmount(), balanceAmount));
//        }
//
//        // Get logged-in user
//        User receivedBy = userRepository.findById(loggedInUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));
//
//        // Create Payment entity
//        Payment payment = new Payment();
//        payment.setStudentFees(studentFees);
//        
//        // ✅ FIX: Get Student from Enrollment
//        payment.setStudent(studentFees.getEnrollment().getStudent());
//        
//        payment.setAmount(request.getAmount());
//        payment.setPaymentMode(request.getPaymentMode());
//        payment.setPaymentDate(request.getPaymentDate());
//        payment.setTransactionId(request.getTransactionId());
//        payment.setReceiptNumber(generateReceiptNumber());
//        payment.setStatus("SUCCESS");
//        payment.setReceivedBy(receivedBy);
//        payment.setRemarks(request.getRemarks());
//
//        Payment savedPayment = paymentRepository.save(payment);
//
//        // Update StudentFees balance
//        studentFees.setPaidAmount(studentFees.getPaidAmount().add(request.getAmount()));
//        studentFees.setBalanceAmount(studentFees.getBalanceAmount().subtract(request.getAmount()));
//        
//        // Check if fully paid
//        if (studentFees.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
//            studentFees.setPaymentStatus("PAID");
//        }
//        
//        studentFeesRepository.save(studentFees);
//
//        // ✅ CREATE AUDIT LOG - Use simplified DTO instead of entity
//        Map<String, Object> paymentData = createPaymentAuditData(savedPayment);
//        auditLogService.createAuditLog("Payment", savedPayment.getPaymentId(), "CREATE", 
//                                       null, paymentData, receivedBy);
//
//        log.info("Payment created successfully with ID: {}", savedPayment.getPaymentId());
//        return mapToResponse(savedPayment);
//    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, Long loggedInUserId) {
        log.info("Creating new payment for fees ID: {}", request.getFeesId());

        StudentFees studentFees = studentFeesRepository.findById(request.getFeesId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "StudentFees not found with ID: " + request.getFeesId()));

        BigDecimal balanceAmount = studentFees.getBalanceAmount();
        if (request.getAmount().compareTo(balanceAmount) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount ₹%.2f exceeds balance amount ₹%.2f",
                            request.getAmount(), balanceAmount));
        }

        User receivedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + loggedInUserId));

        Payment payment = new Payment();
        payment.setStudentFees(studentFees);
        payment.setStudent(studentFees.getEnrollment().getStudent());
        payment.setAmount(request.getAmount());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setTransactionId(request.getTransactionId());
        payment.setReceiptNumber(generateReceiptNumber());
        payment.setStatus("SUCCESS");
        payment.setReceivedBy(receivedBy);
        payment.setRemarks(request.getRemarks());

        Payment savedPayment = paymentRepository.save(payment);

        studentFees.setPaidAmount(studentFees.getPaidAmount().add(request.getAmount()));
        studentFees.setBalanceAmount(studentFees.getBalanceAmount().subtract(request.getAmount()));

        if (studentFees.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
            studentFees.setPaymentStatus("PAID");
        }

        studentFeesRepository.save(studentFees);

        Map<String, Object> paymentData = createPaymentAuditData(savedPayment);
        auditLogService.createAuditLog("Payment", savedPayment.getPaymentId(), "CREATE",
                null, paymentData, receivedBy);

        log.info("Payment created successfully with ID: {}", savedPayment.getPaymentId());

        // ✅ ADD THIS BLOCK — Auto Invoice Generate
        try {
            CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest(savedPayment.getPaymentId());
            invoiceService.generateInvoice(invoiceRequest, loggedInUserId);
            log.info("✅ Invoice auto-generated for payment ID: {}", savedPayment.getPaymentId());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invoice already exists for payment ID: {}", savedPayment.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Auto-invoice failed for payment ID {}: {}",
                    savedPayment.getPaymentId(), e.getMessage());
        }
        // ✅ END OF ADDED BLOCK

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = getPaymentEntityById(paymentId);
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        log.info("Fetching all payments");
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStudent(Long studentId) {
        log.info("Fetching payments for student ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        return paymentRepository.findByStudent(student).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStudentFees(Long feesId) {
        log.info("Fetching payments for fees ID: {}", feesId);
        StudentFees studentFees = studentFeesRepository.findById(feesId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFees not found with ID: " + feesId));
        
        return paymentRepository.findByStudentFees(studentFees).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByReceivedBy(Long userId) {
        log.info("Fetching payments received by user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return paymentRepository.findByReceivedBy(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching payments between {} and {}", startDate, endDate);
        return paymentRepository.findByPaymentDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        log.info("Fetching payments with status: {}", status);
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmountByStudent(Long studentId) {
        log.info("Calculating total paid amount for student ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        BigDecimal totalPaid = paymentRepository.getTotalPaidAmount(student);
        return totalPaid != null ? totalPaid : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalPaymentsCountByReceivedBy(Long userId) {
        log.info("Counting payments received by user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return paymentRepository.countByReceivedBy(user);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, String status, Long loggedInUserId) {
        log.info("Updating payment status for ID: {} to {}", paymentId, status);
        
        Payment payment = getPaymentEntityById(paymentId);
        String oldStatus = payment.getStatus();
        
        // Create old data map for audit
        Map<String, Object> oldData = createPaymentAuditData(payment);
        
        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);

        // Create new data map for audit
        Map<String, Object> newData = createPaymentAuditData(updatedPayment);
        
        // ✅ CREATE AUDIT LOG
        auditLogService.createAuditLog("Payment", paymentId, "UPDATE", oldData, newData, updatedBy);

        log.info("Payment status updated from {} to {}", oldStatus, status);
        return mapToResponse(updatedPayment);
    }

    @Override
    @Transactional
    public void deletePayment(Long paymentId, Long loggedInUserId) {
        log.info("Deleting payment with ID: {}", paymentId);
        
        Payment payment = getPaymentEntityById(paymentId);
        
        // Create data map for audit BEFORE deletion
        Map<String, Object> paymentData = createPaymentAuditData(payment);
        
        // Revert StudentFees balance
        StudentFees studentFees = payment.getStudentFees();
        studentFees.setPaidAmount(studentFees.getPaidAmount().subtract(payment.getAmount()));
        studentFees.setBalanceAmount(studentFees.getBalanceAmount().add(payment.getAmount()));
        
        // Update payment status
        if (studentFees.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            studentFees.setPaymentStatus("PENDING");
        }
        
        studentFeesRepository.save(studentFees);

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // ✅ CREATE AUDIT LOG before deletion
        auditLogService.createAuditLog("Payment", paymentId, "DELETE", paymentData, null, deletedBy);

        paymentRepository.delete(payment);
        log.info("Payment deleted successfully with ID: {}", paymentId);
    }

    @Override
    public Payment getPaymentEntityById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
    }

    // ==================== HELPER METHODS ====================

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());

        // ✅ FIX: Student Info
        Student student = payment.getStudent();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
        response.setStudentName(student.getFullName());

        // Fees Info
        StudentFees fees = payment.getStudentFees();
        response.setFeesId(fees.getFeesId());
        response.setTotalFees(fees.getTotalFees());
        response.setPaidAmount(fees.getPaidAmount());
        response.setBalanceAmount(fees.getBalanceAmount());

        // Payment Info
        response.setAmount(payment.getAmount());
        response.setPaymentMode(payment.getPaymentMode());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTransactionId(payment.getTransactionId());
        response.setReceiptNumber(payment.getReceiptNumber());
        response.setStatus(payment.getStatus());

        // Admin Info
        User receivedBy = payment.getReceivedBy();
        response.setReceivedByUserId(receivedBy.getUserId());
        response.setReceivedByUserName(receivedBy.getFullName());

        response.setRemarks(payment.getRemarks());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }

    /**
     * ✅ NEW: Create simplified audit data map (NO lazy loading issues!)
     */
    private Map<String, Object> createPaymentAuditData(Payment payment) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("paymentId", payment.getPaymentId());
        data.put("amount", payment.getAmount());
        data.put("paymentMode", payment.getPaymentMode());
        data.put("paymentDate", payment.getPaymentDate().toString());
        data.put("transactionId", payment.getTransactionId());
        data.put("receiptNumber", payment.getReceiptNumber());
        data.put("status", payment.getStatus());
        data.put("remarks", payment.getRemarks());
        
        // Student info (without lazy loading)
        Student student = payment.getStudent();
        data.put("studentId", student.getStudentId());
        data.put("studentCode", student.getStudentCode());
        data.put("studentName", student.getFullName());
        
        // Fees info
        StudentFees fees = payment.getStudentFees();
        data.put("feesId", fees.getFeesId());
        data.put("totalFees", fees.getTotalFees());
        data.put("paidAmount", fees.getPaidAmount());
        data.put("balanceAmount", fees.getBalanceAmount());
        
        // Admin info
        User receivedBy = payment.getReceivedBy();
        data.put("receivedByUserId", receivedBy.getUserId());
        data.put("receivedByUserName", receivedBy.getFullName());
        
        data.put("createdAt", payment.getCreatedAt().toString());
        data.put("updatedAt", payment.getUpdatedAt().toString());
        
        return data;
    }

    private String generateReceiptNumber() {
        return "RCP-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
