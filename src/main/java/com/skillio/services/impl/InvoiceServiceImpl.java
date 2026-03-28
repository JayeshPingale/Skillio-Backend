package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CreateInvoiceRequest;
import com.skillio.dto.InvoiceResponse;
import com.skillio.dto.UpdateInvoiceRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Enrollment;
import com.skillio.entities.Invoice;
import com.skillio.entities.Payment;
import com.skillio.entities.Student;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.EnrollmentRepository;
import com.skillio.repositories.InvoiceRepository;
import com.skillio.repositories.PaymentRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.InvoiceService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;
    private final EnrollmentRepository enrollmentRepository; // Add this
    

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(CreateInvoiceRequest request, Long loggedInUserId) {
        log.info("Generating invoice for payment ID: {}", request.getPaymentId());

        // Validate Payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + request.getPaymentId()));

        // Check if invoice already exists for this payment
        invoiceRepository.findByPayment(payment).ifPresent(existingInvoice -> {
            throw new IllegalArgumentException("Invoice already exists for payment ID: " + request.getPaymentId());
        });

        User generatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Invoice entity
        Invoice invoice = new Invoice();
        invoice.setPayment(payment);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setGeneratedDate(LocalDateTime.now());
        invoice.setPdfPath(null); // Will be set after PDF generation
        invoice.setSentToEmail(false);
        invoice.setSentToWhatsApp(false);
        invoice.setRemarks("Auto-generated invoice");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create Audit Log
        createAuditLog("Invoice", savedInvoice.getInvoiceId(), "CREATE", null, savedInvoice, generatedBy);

        log.info("Invoice generated successfully with ID: {} and invoice number: {}", 
                savedInvoice.getInvoiceId(), savedInvoice.getInvoiceNumber());
        return mapToResponse(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        log.info("Fetching invoice with ID: {}", invoiceId);
        Invoice invoice = getInvoiceEntityById(invoiceId);
        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        log.info("Fetching all invoices");
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByPaymentId(Long paymentId) {
        log.info("Fetching invoice for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        Invoice invoice = invoiceRepository.findByPayment(payment)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for payment ID: " + paymentId));

        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByInvoiceNumber(String invoiceNumber) {
        log.info("Fetching invoice with invoice number: {}", invoiceNumber);

        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with invoice number: " + invoiceNumber));

        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getPendingEmailInvoices() {
        log.info("Fetching invoices pending email delivery");
        return invoiceRepository.findBySentToEmailFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getPendingWhatsAppInvoices() {
        log.info("Fetching invoices pending WhatsApp delivery");
        return invoiceRepository.findBySentToWhatsAppFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(Long invoiceId, UpdateInvoiceRequest request, Long loggedInUserId) {
        log.info("Updating invoice with ID: {}", invoiceId);

        Invoice invoice = getInvoiceEntityById(invoiceId);
        Invoice oldInvoice = cloneInvoice(invoice);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Update fields if provided
        if (request.getPdfPath() != null) {
            invoice.setPdfPath(request.getPdfPath());
        }

        if (request.getRemarks() != null) {
            invoice.setRemarks(request.getRemarks());
        }

        Invoice updated = invoiceRepository.save(invoice);

        // Create Audit Log
        createAuditLog("Invoice", invoiceId, "UPDATE", oldInvoice, updated, updatedBy);

        log.info("Invoice updated successfully with ID: {}", invoiceId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsSentToEmail(Long invoiceId, Long loggedInUserId) {
        log.info("Marking invoice {} as sent to email", invoiceId);

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (Boolean.TRUE.equals(invoice.getSentToEmail())) {
            throw new IllegalStateException("Invoice already marked as sent to email");
        }

        Invoice oldInvoice = cloneInvoice(invoice);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        invoice.setSentToEmail(true);
        if (invoice.getSentDate() == null) {
            invoice.setSentDate(LocalDateTime.now());
        }

        Invoice updated = invoiceRepository.save(invoice);

        // Create Audit Log
        createAuditLog("Invoice", invoiceId, "MARK_SENT_EMAIL", oldInvoice, updated, updatedBy);

        log.info("Invoice {} marked as sent to email", invoiceId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsSentToWhatsApp(Long invoiceId, Long loggedInUserId) {
        log.info("Marking invoice {} as sent to WhatsApp", invoiceId);

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (Boolean.TRUE.equals(invoice.getSentToWhatsApp())) {
            throw new IllegalStateException("Invoice already marked as sent to WhatsApp");
        }

        Invoice oldInvoice = cloneInvoice(invoice);

        User updatedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        invoice.setSentToWhatsApp(true);
        if (invoice.getSentDate() == null) {
            invoice.setSentDate(LocalDateTime.now());
        }

        Invoice updated = invoiceRepository.save(invoice);

        // Create Audit Log
        createAuditLog("Invoice", invoiceId, "MARK_SENT_WHATSAPP", oldInvoice, updated, updatedBy);

        log.info("Invoice {} marked as sent to WhatsApp", invoiceId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long invoiceId, Long loggedInUserId) {
        log.info("Deleting invoice with ID: {}", invoiceId);

        Invoice invoice = getInvoiceEntityById(invoiceId);

        User deletedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));

        // Create Audit Log before deletion
        createAuditLog("Invoice", invoiceId, "DELETE", invoice, null, deletedBy);

        invoiceRepository.delete(invoice);
        log.info("Invoice deleted successfully with ID: {}", invoiceId);
    }

    @Override
    public Invoice getInvoiceEntityById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
    }

    @Override
    public String generateInvoiceNumber() {
        // Format: INV_YYYY_NNNN (e.g., INV_2025_0001)
        String year = String.valueOf(LocalDateTime.now().getYear());
        
        // Get last invoice number for current year
        long count = invoiceRepository.count() + 1;
        String sequenceNumber = String.format("%04d", count);
        
        String invoiceNumber = "INV_" + year + "_" + sequenceNumber;
        
        // Check if invoice number already exists (handle race condition)
        while (invoiceRepository.findByInvoiceNumber(invoiceNumber).isPresent()) {
            count++;
            sequenceNumber = String.format("%04d", count);
            invoiceNumber = "INV_" + year + "_" + sequenceNumber;
        }
        
        log.info("Generated invoice number: {}", invoiceNumber);
        return invoiceNumber;
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesBySalesExecutive(Long salesExecutiveId) {
        log.info("Fetching invoices for Sales Executive ID: {}", salesExecutiveId);
        
        // Get all students enrolled by this sales executive
        List<Enrollment> myEnrollments = enrollmentRepository.findByAdmittedBy_UserId(salesExecutiveId);
        
        Set<Long> myStudentIds = myEnrollments.stream()
            .map(e -> e.getStudent().getStudentId())
            .collect(Collectors.toSet());
        
        // Get all invoices for those students
        List<Invoice> invoices = invoiceRepository.findAll().stream()
            .filter(invoice -> {
                Long studentId = invoice.getPayment().getStudent().getStudentId();
                return myStudentIds.contains(studentId);
            })
            .collect(Collectors.toList());
        
        return invoices.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }


    // ==================== HELPER METHODS ====================

    private InvoiceResponse mapToResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceId(invoice.getInvoiceId());
        
        // Payment Info
        Payment payment = invoice.getPayment();
        response.setPaymentId(payment.getPaymentId());
        response.setAmount(payment.getAmount());  // ✅ ADD THIS LINE
        response.setPaymentMode(payment.getPaymentMode());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTransactionId(payment.getTransactionId());
        response.setReceiptNumber(payment.getReceiptNumber());
        
        // Student Info
        Student student = payment.getStudent();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
        response.setStudentName(student.getFullName());
        response.setStudentEmail(student.getEmail());
        
        // Invoice Info
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setGeneratedDate(invoice.getGeneratedDate());
        response.setPdfPath(invoice.getPdfPath());
        response.setSentToEmail(invoice.getSentToEmail());
        response.setSentToWhatsApp(invoice.getSentToWhatsApp());
        response.setRemarks(invoice.getRemarks());
        response.setCreatedAt(invoice.getCreatedAt());
        
        return response;
    }

    private Invoice cloneInvoice(Invoice invoice) {
        Invoice clone = new Invoice();
        clone.setInvoiceId(invoice.getInvoiceId());
        clone.setPayment(invoice.getPayment());
        clone.setInvoiceNumber(invoice.getInvoiceNumber());
        clone.setGeneratedDate(invoice.getGeneratedDate());
        clone.setPdfPath(invoice.getPdfPath());
        clone.setSentToEmail(invoice.getSentToEmail());
        clone.setSentToWhatsApp(invoice.getSentToWhatsApp());
        clone.setSentDate(invoice.getSentDate());
        clone.setRemarks(invoice.getRemarks());
        return clone;
    }

    private void createAuditLog(String entityType, Long entityId, String action,
                                Object oldValue, Object newValue, User performedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            auditLog.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            auditLog.setPerformedBy(performedBy);
            auditLog.setIpAddress(getClientIp());
            auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
            auditLog.setPerformedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} on Invoice ID: {}", action, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }
}
