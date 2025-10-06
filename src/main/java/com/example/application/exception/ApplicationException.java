package com.example.application.exception;

import lombok.Getter;

/**
 * Base exception for application layer errors.
 * Provides structured error information.
 */
@Getter
public class ApplicationException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    private final Object[] parameters;
    
    public ApplicationException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.parameters = new Object[0];
    }
    
    public ApplicationException(String errorCode, String errorMessage, Object... parameters) {
        super(String.format(errorMessage, parameters));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.parameters = parameters;
    }
    
    public ApplicationException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.parameters = new Object[0];
    }
    
    public ApplicationException(String errorCode, String errorMessage, Throwable cause, Object... parameters) {
        super(String.format(errorMessage, parameters), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.parameters = parameters;
    }
}