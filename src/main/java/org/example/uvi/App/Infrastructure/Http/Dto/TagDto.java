package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.Interest.Interest;

import java.time.LocalDateTime;

public record TagDto(
        Long id,
        String name,
        Interest interest,
        String description,
        Integer usageCount,
        LocalDateTime createdAt
) {}
