package org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.InvitationStatus.InvitationStatus;

import java.time.LocalDateTime;

@Schema(description = "Information about a family/group invitation")
public record InvitationDto(
        @Schema(description = "Unique identifier", example = "1")
        Long id,
        @Schema(description = "ID of the family to join", example = "10")
        Long familyId,
        @Schema(description = "Name of the family", example = "The Ivanovs")
        String familyName,
        @Schema(description = "ID of the user who sent the invitation")
        Long inviterId,
        @Schema(description = "Name of the inviter")
        String inviterName,
        @Schema(description = "ID of the user being invited (if exists)")
        Long inviteeId,
        @Schema(description = "Name of the user being invited")
        String inviteeName,
        @Schema(description = "Phone number of the invitee", example = "+79123456789")
        String inviteePhone,
        @Schema(description = "Current invitation status")
        InvitationStatus status,
        @Schema(description = "Unique code to accept the invitation")
        String invitationCode,
        @Schema(description = "Optional message to the invitee")
        String message,
        @Schema(description = "Expiration timestamp")
        LocalDateTime expiresAt,
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        @Schema(description = "Timestamp when the invitee responded")
        LocalDateTime respondedAt
) {}
