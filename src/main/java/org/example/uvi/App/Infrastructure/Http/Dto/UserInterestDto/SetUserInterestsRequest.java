package org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto;

import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.Interest.Interest;

import java.util.Set;

public record SetUserInterestsRequest(
        @NotNull(message = "Interests set is required")
        Set<Interest> interests
) {}
