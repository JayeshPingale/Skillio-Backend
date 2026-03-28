package com.skillio.repositories;

import com.skillio.entities.Payment;
import com.skillio.entities.StudentFees;
import com.skillio.entities.Student;
import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentFees(StudentFees studentFees);
    List<Payment> findByStudent(Student student);
    List<Payment> findByReceivedBy(User receivedBy);
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    List<Payment> findByStatus(String status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.student = :student AND p.status = 'SUCCESS'")
    BigDecimal getTotalPaidAmount(@Param("student") Student student);
    
    // Count queries
    Long countByReceivedBy(User receivedBy);
}
