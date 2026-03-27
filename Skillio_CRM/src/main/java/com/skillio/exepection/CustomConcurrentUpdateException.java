package com.skillio.exepection;

public class CustomConcurrentUpdateException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomConcurrentUpdateException(String message) {
        super(message);
    }
}
