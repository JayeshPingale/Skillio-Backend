package com.skillio.repositories;

import com.skillio.entities.Target;
import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<Target, Long> {
    List<Target> findByUser(User user);
    Optional<Target> findByUserAndStartDateAndEndDate(User user, LocalDate startDate, LocalDate endDate);
    List<Target> findByStatus(String status);
    List<Target> findByEndDateAfter(LocalDate date);
}
