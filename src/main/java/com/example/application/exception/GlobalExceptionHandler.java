package com.example.application.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides centralized error handling and standardized error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles application-specific exceptions.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
            ApplicationException ex, WebRequest request) {
        
        log.warn("Application exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation exception occurred: {}", ex.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.VALIDATION_FAILED)
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("Constraint violation exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.CONSTRAINT_VIOLATION)
                .message("Constraint violation: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handles malformed JSON requests.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.warn("Malformed JSON request: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.INVALID_FORMAT)
                .message("Malformed JSON request")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handles unsupported media type requests.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        log.warn("Unsupported media type: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.INVALID_FORMAT)
                .message("Unsupported media type")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }
    
    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.VALIDATION_FAILED)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handles null pointer exceptions.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        
        log.error("Null pointer exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.REQUIRED_FIELD_MISSING)
                .message("Required field is missing")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handles all other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCodes.INTERNAL_ERROR)
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private ErrorResponse.FieldError mapFieldError(FieldError fieldError) {
        return ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
    
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode.startsWith("MEMBER_") || errorCode.startsWith("ORDER_") || errorCode.startsWith("PAYMENT_")) {
            if (errorCode.endsWith("_001")) { // NOT_FOUND
                return HttpStatus.NOT_FOUND;
            } else if (errorCode.endsWith("_002")) { // ALREADY_EXISTS
                return HttpStatus.CONFLICT;
            } else {
                return HttpStatus.BAD_REQUEST;
            }
        } else if (errorCode.startsWith("VALIDATION_")) {
            return HttpStatus.BAD_REQUEST;
        } else if (errorCode.startsWith("SYSTEM_")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.BAD_REQUEST;
        }
    }
}