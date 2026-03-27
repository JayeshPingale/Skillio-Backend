package com.skillio.exepection;

public class DuplicateBankAccountException extends RuntimeException {
    public DuplicateBankAccountException(String message) {
        super(message);
    }
}
