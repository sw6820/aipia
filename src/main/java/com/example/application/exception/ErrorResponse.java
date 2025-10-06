package com.example.application.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response format for API endpoints.
 * Provides consistent error information structure.
 */
@Getter
@Builder
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String path;
    private String method;
    
    private List<FieldError> fieldErrors;
    
    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String errorCode, String message, String details) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(ApplicationException exception) {
        return ErrorResponse.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}