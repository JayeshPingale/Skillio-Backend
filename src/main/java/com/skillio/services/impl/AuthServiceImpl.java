package com.skillio.services.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.skillio.entities.PasswordResetOtp;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.PasswordResetOtpRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AuditLogService auditLogService;

    @Value("${app.password-reset-otp-expiration-minutes:10}")
    private long otpExpirationMinutes;

    @Value("${app.password-reset-otp-length:5}")
    private int otpLength;

    @Value("${app.password-reset-max-attempts:3}")
    private int maxAttempts;

    @Value("${app.password-reset-cooldown-minutes:10}")
    private long cooldownMinutes;

    @Value("${app.password-reset-mail-from:}")
    private String fromEmail;

    @Override
    @Transactional
    public void sendForgotPasswordOtp(String email) {
        userRepository.findByEmail(email)
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .ifPresent(user -> {
                    enforceCooldownIfLocked(user);
                    invalidateExistingOtps(user);

                    String otp = generateNumericOtp();

                    PasswordResetOtp passwordResetOtp = new PasswordResetOtp();
                    passwordResetOtp.setUser(user);
                    passwordResetOtp.setOtpHash(passwordEncoder.encode(otp));
                    passwordResetOtp.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
                    passwordResetOtp.setFailedAttempts(0);
                    PasswordResetOtp savedOtp = passwordResetOtpRepository.save(passwordResetOtp);

                    sendOtpEmail(user, otp);
                    log.info("Password reset OTP generated for user ID: {}", user.getUserId());
                });
    }

    @Override
    @Transactional
    public void verifyForgotPasswordOtp(String email, String otp) {
        User user = getActiveUserByEmail(email);
        PasswordResetOtp passwordResetOtp = validateOtpOrThrow(user, otp);
        if (passwordResetOtp.getVerifiedAt() == null) {
            passwordResetOtp.setVerifiedAt(LocalDateTime.now());
            passwordResetOtpRepository.save(passwordResetOtp);
            log.info("Password reset OTP verified for user ID: {}", user.getUserId());
        }
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = getActiveUserByEmail(email);
        PasswordResetOtp passwordResetOtp = validateOtpOrThrow(user, otp);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetOtp.setVerifiedAt(LocalDateTime.now());
        passwordResetOtp.setUsedAt(LocalDateTime.now());
        passwordResetOtpRepository.save(passwordResetOtp);

        invalidateOtherOtps(user, passwordResetOtp.getId());
        runAuditAfterCommit(() -> auditLogService.createAuditLog(
                "User",
                user.getUserId(),
                "RESET_PASSWORD",
                null,
                createSafePasswordResetAuditData(user, passwordResetOtp),
                user
        ));
        log.info("Password reset completed for user ID: {}", user.getUserId());
    }

    private User getActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Inactive user cannot reset password");
        }

        return user;
    }

    private PasswordResetOtp validateOtpOrThrow(User user, String otp) {
        PasswordResetOtp passwordResetOtp = passwordResetOtpRepository
                .findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (isLocked(passwordResetOtp)) {
            throw new IllegalStateException(
                    "Too many invalid OTP attempts. Please try again after " + cooldownMinutes + " minutes.");
        }

        if (passwordResetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (!passwordEncoder.matches(otp, passwordResetOtp.getOtpHash())) {
            registerFailedAttempt(passwordResetOtp);
        }

        return passwordResetOtp;
    }

    private void enforceCooldownIfLocked(User user) {
        passwordResetOtpRepository.findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .filter(this::isLocked)
                .ifPresent(otp -> {
                    throw new IllegalStateException(
                            "Too many invalid OTP attempts. Please try again after " + cooldownMinutes + " minutes.");
                });
    }

    private void invalidateExistingOtps(User user) {
        List<PasswordResetOtp> activeOtps = passwordResetOtpRepository.findByUserAndUsedAtIsNull(user);
        if (activeOtps.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        activeOtps.forEach(otp -> otp.setUsedAt(now));
        passwordResetOtpRepository.saveAll(activeOtps);
    }

    private void invalidateOtherOtps(User user, Long currentOtpId) {
        List<PasswordResetOtp> activeOtps = passwordResetOtpRepository.findByUserAndUsedAtIsNull(user);
        if (activeOtps.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        activeOtps.stream()
                .filter(otp -> !otp.getId().equals(currentOtpId))
                .forEach(otp -> otp.setUsedAt(now));

        passwordResetOtpRepository.saveAll(activeOtps);
    }

    private void registerFailedAttempt(PasswordResetOtp passwordResetOtp) {
        int allowedAttempts = Math.max(1, maxAttempts);
        int nextAttemptCount = (passwordResetOtp.getFailedAttempts() == null ? 0 : passwordResetOtp.getFailedAttempts()) + 1;
        passwordResetOtp.setFailedAttempts(nextAttemptCount);
        User user = passwordResetOtp.getUser();

        if (nextAttemptCount >= allowedAttempts) {
            passwordResetOtp.setLockedUntil(LocalDateTime.now().plusMinutes(cooldownMinutes));
            passwordResetOtpRepository.save(passwordResetOtp);
            throw new IllegalStateException(
                    "Too many invalid OTP attempts. Please try again after " + cooldownMinutes + " minutes.");
        }

        passwordResetOtpRepository.save(passwordResetOtp);
        throw new IllegalArgumentException(
                "Invalid OTP. " + (allowedAttempts - nextAttemptCount) + " attempt(s) remaining.");
    }

    private boolean isLocked(PasswordResetOtp passwordResetOtp) {
        return passwordResetOtp.getLockedUntil() != null
                && passwordResetOtp.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private Map<String, Object> createSafeOtpAuditData(PasswordResetOtp passwordResetOtp, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("otpId", passwordResetOtp.getId());
        data.put("userId", passwordResetOtp.getUser().getUserId());
        data.put("email", passwordResetOtp.getUser().getEmail());
        data.put("status", status);
        data.put("failedAttempts", passwordResetOtp.getFailedAttempts());
        data.put("expiresAt", passwordResetOtp.getExpiresAt());
        data.put("verifiedAt", passwordResetOtp.getVerifiedAt());
        data.put("lockedUntil", passwordResetOtp.getLockedUntil());
        data.put("usedAt", passwordResetOtp.getUsedAt());
        return data;
    }

    private Map<String, Object> createSafePasswordResetAuditData(User user, PasswordResetOtp passwordResetOtp) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("email", user.getEmail());
        data.put("passwordResetAt", LocalDateTime.now());
        data.put("otpId", passwordResetOtp.getId());
        data.put("otpVerifiedAt", passwordResetOtp.getVerifiedAt());
        return data;
    }

    private void runAuditAfterCommit(Runnable auditTask) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safelyRunAudit(auditTask);
                }
            });
            return;
        }
        safelyRunAudit(auditTask);
    }

    private void safelyRunAudit(Runnable auditTask) {
        try {
            auditTask.run();
        } catch (Exception ex) {
            log.error("Audit logging failed in auth flow: {}", ex.getMessage(), ex);
        }
    }

    private String generateNumericOtp() {
        int length = Math.max(4, otpLength);
        int upperBound = (int) Math.pow(10, length);
        String otp = String.valueOf(SECURE_RANDOM.nextInt(upperBound));
        return String.format("%0" + length + "d", Integer.parseInt(otp));
    }

    private void sendOtpEmail(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(user.getEmail());
            message.setSubject("Skillio CRM Password Reset OTP");
            message.setText(buildOtpEmailBody(user, otp));
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password reset OTP email to {}", user.getEmail(), ex);
            throw new IllegalStateException(
                    "Password reset email could not be sent. Please configure Spring Mail SMTP settings.");
        }
    }

    private String buildOtpEmailBody(User user, String otp) {
        return "Hello " + user.getFullName() + ",\n\n"
                + "Your Skillio CRM password reset OTP is: " + otp + "\n"
                + "This OTP will expire in " + otpExpirationMinutes + " minutes.\n\n"
                + "If you did not request a password reset, you can ignore this email.\n\n"
                + "Regards,\nSkillio CRM";
    }
}
