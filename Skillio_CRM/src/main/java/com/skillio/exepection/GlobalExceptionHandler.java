package com.skillio.exepection;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        logger.error("Resource not found: {}", ex.getMessage());
        APIError err = new APIError();
        err.setStatus(HttpStatus.NOT_FOUND.value());
        err.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        err.setMessage(ex.getMessage());
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        logger.error("Authentication failed: {}", ex.getMessage());
        APIError err = new APIError();
        err.setStatus(HttpStatus.UNAUTHORIZED.value());
        err.setError("Unauthorized");
        err.setMessage("Invalid email or password");
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        APIError err = new APIError();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        err.setMessage("Validation failed");
        err.setPath(req.getRequestURI());
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
        err.setValidationErrors(details);
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIError> handleState(IllegalStateException ex, HttpServletRequest req) {
        APIError err = new APIError();
        err.setStatus(HttpStatus.CONFLICT.value());
        err.setError(HttpStatus.CONFLICT.getReasonPhrase());
        err.setMessage(ex.getMessage());
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        APIError err = new APIError();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        err.setMessage(ex.getMessage());
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    // ✅ NEW: Handle EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        logger.error("❌ Entity Not Found: {}", ex.getMessage());
        APIError err = new APIError();
        err.setStatus(HttpStatus.NOT_FOUND.value());
        err.setError("Not Found");
        err.setMessage("The requested resource could not be found");
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    // ✅ NEW: Handle DataIntegrityViolationException (SQL errors)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest req) {
        logger.error("❌ Data Integrity Violation: {}", ex.getMessage());
        
        APIError err = new APIError();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError("Data Validation Error");
        err.setPath(req.getRequestURI());
        
        // ✅ Parse SQL error and show user-friendly message
        String message = "Failed to save data due to validation error";
        
        String rootCause = ex.getMostSpecificCause().getMessage();
        
        if (rootCause.contains("cannot be null")) {
            if (rootCause.contains("sales_executive_id")) {
                message = "Sales Executive assignment is required";
            } else if (rootCause.contains("course_interested")) {
                message = "Course of interest is required";
            } else if (rootCause.contains("source_id")) {
                message = "Lead source is required";
            } else {
                message = "Required field is missing";
            }
        } else if (rootCause.contains("Duplicate entry")) {
            if (rootCause.contains("contact_number")) {
                message = "This contact number already exists";
            } else if (rootCause.contains("email")) {
                message = "This email already exists";
            } else {
                message = "This record already exists in the database";
            }
        } else if (rootCause.contains("foreign key constraint")) {
            message = "Cannot delete this record as it is being used elsewhere";
        }
        
        err.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // ✅ Handle Email Duplicate
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Email");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "email");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ✅ Handle Phone Duplicate
    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Phone Number");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "phone");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ✅ Handle Bank Account Duplicate
    @ExceptionHandler(DuplicateBankAccountException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateBankAccount(DuplicateBankAccountException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Bank Account");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "bankAccountNo");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomConcurrentUpdateException.class)
    public ResponseEntity<String> handleConcurrentUpdateException(CustomConcurrentUpdateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }

    @ExceptionHandler(UserApiException.class)
    public ResponseEntity<?> handleUserApiException(UserApiException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", ex.getMessage()));
    }

    // ✅ Generic Exception Handler (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIError> handleGeneric(Exception ex, HttpServletRequest req) {
        logger.error("❌ Unexpected Error: {}", ex.getMessage(), ex);
        
        APIError err = new APIError();
        err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        err.setMessage("An unexpected error occurred. Please try again later.");
        err.setPath(req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}


