package com.skillio.repositories;

import com.skillio.entities.PaymentInstallment;
import com.skillio.entities.StudentFees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, Long> {
    List<PaymentInstallment> findByStudentFees(StudentFees studentFees);
    List<PaymentInstallment> findByStatus(String status);
    List<PaymentInstallment> findByDueDateBefore(LocalDate date);
    List<PaymentInstallment> findByDueDateBeforeAndStatus(LocalDate date, String status); // For OVERDUE
}
