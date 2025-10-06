package com.example.application.exception;

/**
 * Centralized error codes for the application.
 * Provides consistent error identification across the system.
 */
public final class ErrorCodes {
    
    // Member related errors
    public static final String MEMBER_NOT_FOUND = "MEMBER_001";
    public static final String MEMBER_ALREADY_EXISTS = "MEMBER_002";
    public static final String MEMBER_INVALID_EMAIL = "MEMBER_003";
    public static final String MEMBER_INVALID_PHONE = "MEMBER_004";
    public static final String MEMBER_ALREADY_ACTIVE = "MEMBER_005";
    public static final String MEMBER_ALREADY_INACTIVE = "MEMBER_006";
    
    // Order related errors
    public static final String ORDER_NOT_FOUND = "ORDER_001";
    public static final String ORDER_ALREADY_EXISTS = "ORDER_002";
    public static final String ORDER_INVALID_STATUS = "ORDER_003";
    public static final String ORDER_CANNOT_BE_CANCELLED = "ORDER_004";
    public static final String ORDER_CANNOT_BE_COMPLETED = "ORDER_005";
    public static final String ORDER_ITEM_INVALID = "ORDER_006";
    public static final String ORDER_TOTAL_MISMATCH = "ORDER_007";
    
    // Payment related errors
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_001";
    public static final String PAYMENT_ALREADY_EXISTS = "PAYMENT_002";
    public static final String PAYMENT_INVALID_STATUS = "PAYMENT_003";
    public static final String PAYMENT_CANNOT_BE_PROCESSED = "PAYMENT_004";
    public static final String PAYMENT_AMOUNT_MISMATCH = "PAYMENT_005";
    public static final String PAYMENT_METHOD_NOT_SUPPORTED = "PAYMENT_006";
    
    // Validation errors
    public static final String VALIDATION_FAILED = "VALIDATION_001";
    public static final String REQUIRED_FIELD_MISSING = "VALIDATION_002";
    public static final String INVALID_FORMAT = "VALIDATION_003";
    public static final String CONSTRAINT_VIOLATION = "VALIDATION_004";
    
    // System errors
    public static final String INTERNAL_ERROR = "SYSTEM_001";
    public static final String DATABASE_ERROR = "SYSTEM_002";
    public static final String EXTERNAL_SERVICE_ERROR = "SYSTEM_003";
    public static final String CONCURRENT_MODIFICATION = "SYSTEM_004";
    
    private ErrorCodes() {
        // Utility class
    }
}