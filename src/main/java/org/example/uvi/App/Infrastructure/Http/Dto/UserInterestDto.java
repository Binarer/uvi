package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.Interest.Interest;

import java.time.LocalDateTime;

public record UserInterestDto(
        Long id,
        Interest interest,
        String interestDisplayName,
        Integer preferenceLevel,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt
) {}
