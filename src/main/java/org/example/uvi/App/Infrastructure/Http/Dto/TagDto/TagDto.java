package org.example.uvi.App.Infrastructure.Http.Dto.TagDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.Interest.Interest;

import java.time.LocalDateTime;

@Schema(description = "Information about a tag that can be attached to places")
public record TagDto(
        @Schema(description = "Unique identifier", example = "1")
        Long id,
        @Schema(description = "Name of the tag", example = "Family Friendly")
        String name,
        @Schema(description = "Primary interest category this tag belongs to", example = "FAMILY")
        Interest interest,
        @Schema(description = "Description of the tag", example = "Places suitable for visiting with children")
        String description,
        @Schema(description = "How many places currently use this tag", example = "15")
        Integer usageCount,
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
