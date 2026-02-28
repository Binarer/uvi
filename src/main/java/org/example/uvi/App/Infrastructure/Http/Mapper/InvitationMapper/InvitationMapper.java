package org.example.uvi.App.Infrastructure.Http.Mapper.InvitationMapper;

import org.example.uvi.App.Domain.Models.Family.FamilyInvitation;
import org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationDto toDto(FamilyInvitation invitation) {
        if (invitation == null) return null;
        return new InvitationDto(
                invitation.getId(),
                invitation.getFamily().getId(),
                invitation.getFamily().getName(),
                invitation.getInviter().getId(),
                invitation.getInviter().getFirstName() + " " +
                        (invitation.getInviter().getLastName() != null ? invitation.getInviter().getLastName() : ""),
                invitation.getInvitee() != null ? invitation.getInvitee().getId() : null,
                invitation.getInvitee() != null ? invitation.getInvitee().getFirstName() : null,
                invitation.getInviteePhone(),
                invitation.getStatus(),
                invitation.getInvitationCode(),
                invitation.getMessage(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getRespondedAt()
        );
    }
}
