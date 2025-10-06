package com.example.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

/**
 * Command object for updating member information.
 */
@Data
@Builder
public class UpdateMemberCommand {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
}