package org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.FamilyStatus.FamilyStatus;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Information about a family/group")
public record FamilyDto(
        @Schema(description = "Unique identifier", example = "1")
        Long id,
        @Schema(description = "Name of the family", example = "The Ivanovs")
        String name,
        @Schema(description = "Brief description", example = "Our weekend travel group")
        String description,
        @Schema(description = "URL to the family avatar/icon")
        String avatarUrl,
        @Schema(description = "Current family status")
        FamilyStatus status,
        @Schema(description = "ID of the family creator")
        Long creatorId,
        @Schema(description = "Name of the family creator")
        String creatorName,
        @Schema(description = "Maximum allowed members", example = "10")
        Integer maxMembers,
        @Schema(description = "Current number of members", example = "3")
        int memberCount,
        @Schema(description = "List of current family members")
        List<FamilyMemberDto> members,
        @Schema(description = "Timestamp when the family was created")
        LocalDateTime createdAt
) {}
