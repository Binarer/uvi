package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFamilyRequest(
        @NotBlank(message = "Family name is required")
        @Size(min = 1, max = 200, message = "Family name must be between 1 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {}
