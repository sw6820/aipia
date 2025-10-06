package com.example.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value object representing a phone number.
 * Immutable and self-validating with format validation.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PhoneNumber {
    
    // Korean phone number pattern: XXX-XXXX-XXXX
    private static final Pattern KOREAN_PHONE_PATTERN = Pattern.compile(
        "^\\d{3}-\\d{4}-\\d{4}$"
    );
    
    // International phone number pattern (basic)
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private String value;
    private String countryCode;
    
    public PhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
        
        String trimmedPhone = phoneNumber.trim();
        
        if (KOREAN_PHONE_PATTERN.matcher(trimmedPhone).matches()) {
            this.value = trimmedPhone;
            this.countryCode = "KR";
        } else if (INTERNATIONAL_PHONE_PATTERN.matcher(trimmedPhone).matches()) {
            this.value = trimmedPhone;
            this.countryCode = "INT";
        } else {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
    }
    
    public PhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Country code cannot be null or empty");
        }
        
        String trimmedPhone = phoneNumber.trim();
        String trimmedCountryCode = countryCode.trim().toUpperCase();
        
        // Validate phone number format based on country code
        if ("KR".equals(trimmedCountryCode)) {
            if (!KOREAN_PHONE_PATTERN.matcher(trimmedPhone).matches()) {
                throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
            }
        } else if ("INT".equals(trimmedCountryCode)) {
            if (!INTERNATIONAL_PHONE_PATTERN.matcher(trimmedPhone).matches()) {
                throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
            }
        } else {
            throw new IllegalArgumentException("Unsupported country code: " + countryCode);
        }
        
        this.value = trimmedPhone;
        this.countryCode = trimmedCountryCode;
    }
    
    /**
     * Factory method for creating Korean phone number.
     */
    public static PhoneNumber korean(String phoneNumber) {
        return new PhoneNumber(phoneNumber, "KR");
    }
    
    /**
     * Factory method for creating international phone number.
     */
    public static PhoneNumber international(String phoneNumber) {
        PhoneNumber phone = new PhoneNumber(phoneNumber);
        if (!phone.isInternational()) {
            throw new IllegalArgumentException("Invalid international phone number format: " + phoneNumber);
        }
        return phone;
    }
    
    /**
     * Checks if this is a Korean phone number.
     */
    public boolean isKorean() {
        return "KR".equals(countryCode);
    }
    
    /**
     * Checks if this is an international phone number.
     */
    public boolean isInternational() {
        return "INT".equals(countryCode);
    }
    
    /**
     * Gets the formatted phone number for display.
     */
    public String getFormatted() {
        if (isKorean()) {
            return value; // Already formatted as XXX-XXXX-XXXX
        } else {
            // For international numbers, add + prefix only if not already present
            return value.startsWith("+") ? value : "+" + value;
        }
    }
    
    /**
     * Gets the phone number without formatting.
     */
    public String getDigitsOnly() {
        return value.replaceAll("[^0-9]", "");
    }
    
    /**
     * Gets the area code for Korean numbers.
     */
    public String getAreaCode() {
        if (isKorean()) {
            return value.substring(0, 3);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return getFormatted();
    }
}