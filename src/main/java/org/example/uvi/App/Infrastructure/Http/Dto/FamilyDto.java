package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.FamilyStatus.FamilyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FamilyDto(
        Long id,
        String name,
        String description,
        String avatarUrl,
        FamilyStatus status,
        Long creatorId,
        String creatorName,
        Integer maxMembers,
        int memberCount,
        List<FamilyMemberDto> members,
        LocalDateTime createdAt
) {}
