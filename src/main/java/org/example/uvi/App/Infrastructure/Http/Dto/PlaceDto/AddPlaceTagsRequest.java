package org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AddPlaceTagsRequest(
        @NotEmpty(message = "At least one tag ID is required")
        Set<Long> tagIds
) {}
