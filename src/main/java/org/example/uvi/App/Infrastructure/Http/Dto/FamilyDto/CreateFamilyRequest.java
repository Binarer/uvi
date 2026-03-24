package org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new family/group")
public record CreateFamilyRequest(
        @NotBlank(message = "Family name is required")
        @Size(min = 1, max = 200, message = "Family name must be between 1 and 200 characters")
        @Schema(description = "Name of the family", example = "The Ivanovs")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(description = "Brief description of the family", example = "Our weekend travel group")
        String description,

        @Schema(description = "URL to the family avatar/icon", example = "https://example.com/avatar.jpg")
        String avatarUrl
) {}
