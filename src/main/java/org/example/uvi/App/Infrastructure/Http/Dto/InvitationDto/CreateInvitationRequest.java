package org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format")
        String inviteePhone,

        @Size(max = 500, message = "Message must not exceed 500 characters")
        String message
) {}
