package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.InvitationStatus.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationDto(
        Long id,
        Long familyId,
        String familyName,
        Long inviterId,
        String inviterName,
        Long inviteeId,
        String inviteeName,
        String inviteePhone,
        InvitationStatus status,
        String invitationCode,
        String message,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {}
