package org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto;

import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;

import java.time.LocalDateTime;

public record FamilyMemberDto(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String phoneNumber,
        FamilyMemberRole role,
        String displayName,
        Boolean isActive,
        LocalDateTime joinedAt
) {}
