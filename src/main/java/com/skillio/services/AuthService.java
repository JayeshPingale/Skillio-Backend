package com.skillio.services;

public interface AuthService {

    void sendForgotPasswordOtp(String email);

    void verifyForgotPasswordOtp(String email, String otp);

    void resetPassword(String email, String otp, String newPassword, String confirmPassword);
}
