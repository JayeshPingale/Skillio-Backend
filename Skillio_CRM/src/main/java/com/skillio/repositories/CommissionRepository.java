package com.skillio.repositories;

import com.skillio.entities.Commission;
import com.skillio.entities.User;
import com.skillio.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    List<Commission> findBySalesExecutive(User salesExecutive);
    List<Commission> findBySalesExecutiveAndStatus(User salesExecutive, String status);
    List<Commission> findByStatus(String status);
    List<Commission> findByEnrollment(Enrollment enrollment);
    List<Commission> findByEnrollmentOrderByCreatedAtDesc(Enrollment enrollment);
    
    @Query("SELECT SUM(c.eligibleAmount) FROM Commission c WHERE c.salesExecutive = :salesExecutive AND c.status = 'ELIGIBLE'")
    BigDecimal getTotalEligibleAmount(@Param("salesExecutive") User salesExecutive);
    
    @Query("SELECT SUM(cp.amountPaid) FROM Commission c JOIN CommissionPayment cp ON c.commissionId = cp.commission.commissionId WHERE c.salesExecutive = :salesExecutive")
    BigDecimal getTotalPaidCommission(@Param("salesExecutive") User salesExecutive);
    
    Long countBySalesExecutiveAndStatus(User salesExecutive, String status);
}
