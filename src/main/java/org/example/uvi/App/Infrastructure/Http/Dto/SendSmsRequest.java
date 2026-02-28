package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendSmsRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format. Expected: 79XXXXXXXXX")
        String phoneNumber
) {}
