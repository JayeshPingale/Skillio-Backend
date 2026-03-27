package com.skillio.repositories;

import com.skillio.entities.Invoice;
import com.skillio.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByPayment(Payment payment);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findBySentToEmailFalse();
    List<Invoice> findBySentToWhatsAppFalse();
}
