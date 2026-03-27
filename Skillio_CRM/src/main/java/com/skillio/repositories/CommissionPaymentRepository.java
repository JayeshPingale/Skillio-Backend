package com.skillio.repositories;

import com.skillio.entities.CommissionPayment;
import com.skillio.entities.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {
    Optional<CommissionPayment> findByCommission(Commission commission);
    List<CommissionPayment> findByPaidBy_UserId(Long userId);
}
