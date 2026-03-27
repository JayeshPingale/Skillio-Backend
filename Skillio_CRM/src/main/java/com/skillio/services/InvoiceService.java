package com.skillio.services;

import com.skillio.dto.CreateInvoiceRequest;
import com.skillio.dto.UpdateInvoiceRequest;
import com.skillio.dto.InvoiceResponse;
import com.skillio.entities.Invoice;

import java.util.List;

public interface InvoiceService {
    
    // Create
    InvoiceResponse generateInvoice(CreateInvoiceRequest request, Long loggedInUserId);
    
    // Read
    InvoiceResponse getInvoiceById(Long invoiceId);
    List<InvoiceResponse> getAllInvoices();
    InvoiceResponse getInvoiceByPaymentId(Long paymentId);
    InvoiceResponse getInvoiceByInvoiceNumber(String invoiceNumber);
    List<InvoiceResponse> getPendingEmailInvoices();
    List<InvoiceResponse> getPendingWhatsAppInvoices();
    
    // Update
    InvoiceResponse updateInvoice(Long invoiceId, UpdateInvoiceRequest request, Long loggedInUserId);
    InvoiceResponse markAsSentToEmail(Long invoiceId, Long loggedInUserId);
    InvoiceResponse markAsSentToWhatsApp(Long invoiceId, Long loggedInUserId);
    
    // Delete
    void deleteInvoice(Long invoiceId, Long loggedInUserId);
    
    // Helper
    Invoice getInvoiceEntityById(Long invoiceId);
    String generateInvoiceNumber();
    
    List<InvoiceResponse> getInvoicesBySalesExecutive(Long salesExecutiveId);

}
