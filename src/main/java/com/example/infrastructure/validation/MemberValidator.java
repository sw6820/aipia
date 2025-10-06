package com.example.infrastructure.validation;

import com.example.domain.valueobject.Email;
import com.example.domain.valueobject.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for member-related operations.
 * Provides business rule validation separate from domain entities.
 */
@Component
@Slf4j
public class MemberValidator {

    /**
     * Validates member creation data.
     */
    public void validateMemberCreation(String email, String name, String phoneNumber) {
        log.debug("Validating member creation data");
        
        // Validate email format
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        try {
            Email.of(email);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid email format: " + email, e);
        }
        
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
        
        // Validate phone number
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        try {
            PhoneNumber.korean(phoneNumber);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber, e);
        }
        
        log.debug("Member creation data validation passed");
    }

    /**
     * Validates member update data.
     */
    public void validateMemberUpdate(String name, String phoneNumber) {
        log.debug("Validating member update data");
        
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
        
        // Validate phone number
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        try {
            PhoneNumber.korean(phoneNumber);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber, e);
        }
        
        log.debug("Member update data validation passed");
    }

    /**
     * Validates member ID.
     */
    public void validateMemberId(Long memberId) {
        log.debug("Validating member ID: {}", memberId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        if (memberId <= 0) {
            throw new IllegalArgumentException("Member ID must be positive");
        }
        
        log.debug("Member ID validation passed");
    }
}