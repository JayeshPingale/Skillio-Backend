package com.skillio.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillio.entities.PasswordResetOtp;
import com.skillio.entities.User;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    List<PasswordResetOtp> findByUserAndUsedAtIsNull(User user);

    Optional<PasswordResetOtp> findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);
}
