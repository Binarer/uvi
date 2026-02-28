package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.Interest.Interest;

public record AddUserInterestRequest(
        @NotNull(message = "Interest is required")
        Interest interest,

        @Min(value = 1, message = "Preference level must be at least 1")
        @Max(value = 10, message = "Preference level must be at most 10")
        int preferenceLevel
) {}
