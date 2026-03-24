package org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto;

import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;

public record AddFamilyMemberRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Role is required")
        FamilyMemberRole role
) {}
