package org.example.uvi.App.Infrastructure.Http.Mapper.FamilyMapper;

import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.FamilyDto;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.FamilyMemberDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FamilyMapper {

    public FamilyDto toDto(Family family) {
        if (family == null) return null;
        List<FamilyMember> activeMembers = family.getMembers().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .toList();

        return new FamilyDto(
                family.getId(),
                family.getName(),
                family.getDescription(),
                family.getAvatarUrl(),
                family.getStatus(),
                family.getCreator().getId(),
                family.getCreator().getFirstName() + " " +
                        (family.getCreator().getLastName() != null ? family.getCreator().getLastName() : ""),
                family.getMaxMembers(),
                activeMembers.size(),
                activeMembers.stream().map(this::toMemberDto).toList(),
                family.getCreatedAt()
        );
    }

    public FamilyMemberDto toMemberDto(FamilyMember member) {
        if (member == null) return null;
        return new FamilyMemberDto(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getFirstName(),
                member.getUser().getLastName(),
                member.getUser().getPhoneNumber(),
                member.getRole(),
                member.getDisplayName(),
                member.getIsActive(),
                member.getJoinedAt()
        );
    }
}
