package com.skillio.exepection;

public class UserApiException extends RuntimeException {
    public UserApiException(String message) {
        super(message);
    }
}
