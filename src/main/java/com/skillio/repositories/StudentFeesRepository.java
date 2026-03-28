package com.skillio.repositories;

import com.skillio.entities.StudentFees;
import com.skillio.entities.Enrollment;
import com.skillio.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface StudentFeesRepository extends JpaRepository<StudentFees, Long> {

    Optional<StudentFees> findByEnrollment(Enrollment enrollment);

    List<StudentFees> findByPaymentStatus(String paymentStatus);

    List<StudentFees> findByPaymentStatusOrderByDueDateAsc(String paymentStatus);
    
    // Additional useful queries
    List<StudentFees> findByDueDateBeforeAndPaymentStatusNot(LocalDate date, String paymentStatus);
    
    @Query("SELECT sf FROM StudentFees sf WHERE sf.enrollment.student = :student")
    List<StudentFees> findByStudent(@Param("student") Student student);
}
