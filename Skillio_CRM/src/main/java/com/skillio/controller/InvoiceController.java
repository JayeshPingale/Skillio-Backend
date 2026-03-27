package com.skillio.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.ApiResponse;
import com.skillio.dto.CreateInvoiceRequest;
import com.skillio.dto.InvoiceResponse;
import com.skillio.entities.Invoice;
import com.skillio.entities.Payment;
import com.skillio.entities.Student;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.InvoiceRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        InvoiceResponse response = invoiceService.generateInvoice(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICE_LIST')")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long invoiceId) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(invoice);
    }
    
    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasAuthority('INVOICE_DELETE')")
    public ResponseEntity<Map<String, String>> deleteInvoice(
            @PathVariable Long invoiceId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        invoiceService.deleteInvoice(invoiceId, loggedInUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invoice deleted successfully");
        return ResponseEntity.ok(response);
    }

    
    // ✅ MOVE THIS BEFORE /{invoiceId}
    @GetMapping("/my-students-invoices")
    @PreAuthorize("hasAuthority('INVOICE_LIST')")
    public ResponseEntity<List<InvoiceResponse>> getMyStudentsInvoices(
            Authentication authentication) {
        
        Long salesExecutiveId = extractUserId(authentication);
        log.info("Sales Executive {} requesting student invoices", salesExecutiveId);
        
        List<InvoiceResponse> invoices = invoiceService.getInvoicesBySalesExecutive(salesExecutiveId);
        return ResponseEntity.ok(invoices);
    }
    // ==================== PDF OPERATIONS ====================

    @GetMapping("/{id}/download-pdf")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ResponseEntity<Resource> downloadInvoicePDF(@PathVariable Long id) {
        log.info("📥 Downloading PDF for invoice ID: {}", id);
        
        try {
            Invoice invoice = invoiceService.getInvoiceEntityById(id);
            
            if (invoice.getPdfPath() == null || invoice.getPdfPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            File pdfFile = new File(invoice.getPdfPath());
            
            if (!pdfFile.exists()) {
                createDummyPDF(pdfFile, invoice);  // ✅ Pass invoice object
            }
            
            if (!pdfFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(pdfFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/generate-pdf")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ResponseEntity<ApiResponse> generateInvoicePDF(@PathVariable Long id) {
        log.info("📄 Generating PDF for invoice ID: {}", id);
        
        try {
            Invoice invoice = invoiceService.getInvoiceEntityById(id);
            
            String projectRoot = System.getProperty("user.dir");
            Path invoicesDir = Paths.get(projectRoot, "invoices", "pdf");
            Files.createDirectories(invoicesDir);
            
            String pdfPath = invoicesDir.resolve(invoice.getInvoiceNumber() + ".pdf").toString();
            File pdfFile = new File(pdfPath);
            
            createDummyPDF(pdfFile, invoice);  // ✅ Pass invoice object
            
            invoice.setPdfPath(pdfPath);
            invoiceRepository.save(invoice);
            
            return ResponseEntity.ok(new ApiResponse("PDF generated", true, pdfPath));
            
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed: " + e.getMessage(), false));
        }
    }


    // ==================== HELPER ====================

    /**
     * ✅ Create Better Dummy PDF with Invoice Details
     */
    private void createDummyPDF(File pdfFile, Invoice invoice) {
        try {
            pdfFile.getParentFile().mkdirs();
            pdfFile.createNewFile();
            
            Payment payment = invoice.getPayment();
            Student student = payment.getStudent();
            
            try (FileWriter writer = new FileWriter(pdfFile)) {
                // Minimal PDF Header
                writer.write("%PDF-1.4\n");
                
                // Catalog
                writer.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
                
                // Pages
                writer.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
                
                // Page
                writer.write("3 0 obj\n");
                writer.write("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ");
                writer.write("/Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> ");
                writer.write("/Contents 4 0 R >>\n");
                writer.write("endobj\n");
                
                // Content with Invoice Details
                StringBuilder content = new StringBuilder();
                content.append("BT\n");
                content.append("/F1 20 Tf\n");
                content.append("100 750 Td\n");
                content.append("(PAYMENT INVOICE) Tj\n");
                
                content.append("0 -30 Td\n");
                content.append("/F1 12 Tf\n");
                content.append("(Invoice Number: ").append(invoice.getInvoiceNumber()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Student: ").append(student.getFullName()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Student Code: ").append(student.getStudentCode()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Amount: Rs. ").append(payment.getAmount()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Payment Mode: ").append(payment.getPaymentMode()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Receipt No: ").append(payment.getReceiptNumber()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Payment Date: ").append(payment.getPaymentDate()).append(") Tj\n");
                
                content.append("0 -20 Td\n");
                content.append("(Generated Date: ").append(invoice.getGeneratedDate()).append(") Tj\n");
                
                content.append("ET\n");
                
                String contentStr = content.toString();
                writer.write("4 0 obj\n");
                writer.write("<< /Length " + contentStr.length() + " >>\n");
                writer.write("stream\n");
                writer.write(contentStr);
                writer.write("\nendstream\n");
                writer.write("endobj\n");
                
                // XRef Table
                writer.write("xref\n");
                writer.write("0 5\n");
                writer.write("0000000000 65535 f\n");
                writer.write("0000000009 00000 n\n");
                writer.write("0000000058 00000 n\n");
                writer.write("0000000115 00000 n\n");
                writer.write("0000000300 00000 n\n");
                
                // Trailer
                writer.write("trailer\n");
                writer.write("<< /Size 5 /Root 1 0 R >>\n");
                writer.write("startxref\n");
                writer.write(String.valueOf(600 + contentStr.length()));
                writer.write("\n%%EOF\n");
            }
            
            log.info("✅ Invoice PDF created: {}", pdfFile.getAbsolutePath());
            
        } catch (IOException e) {
            log.error("❌ Failed to create invoice PDF: {}", e.getMessage());
        }
    }


    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return user.getUserId();
    }
}
