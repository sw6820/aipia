package com.example.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * Immutable and self-validating.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private String value;
    
    public Email(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        this.value = trimmedEmail;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Factory method for creating Email from string.
     */
    public static Email of(String email) {
        return new Email(email);
    }
    
    /**
     * Checks if this email has a specific domain.
     */
    public boolean hasDomain(String domain) {
        return value.endsWith("@" + domain.toLowerCase());
    }
    
    /**
     * Gets the local part of the email (before @).
     */
    public String getLocalPart() {
        return value.split("@")[0];
    }
    
    /**
     * Gets the domain part of the email (after @).
     */
    public String getDomainPart() {
        return value.split("@")[1];
    }
}