package org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.Interest.Interest;

import java.time.LocalDateTime;

@Schema(description = "Information about a user's interest and preference level")
public record UserInterestDto(
        @Schema(description = "Unique identifier of the interest record", example = "1")
        Long id,
        @Schema(description = "The interest enum value", example = "SPORTS")
        Interest interest,
        @Schema(description = "Human-readable display name of the interest", example = "Sports & Fitness")
        String interestDisplayName,
        @Schema(description = "Preference level (higher means more interested)", example = "5")
        Integer preferenceLevel,
        @Schema(description = "Timestamp when the interest was added")
        LocalDateTime createdAt,
        @Schema(description = "Timestamp when the interest was last used for recommendations")
        LocalDateTime lastUsedAt
) {}
