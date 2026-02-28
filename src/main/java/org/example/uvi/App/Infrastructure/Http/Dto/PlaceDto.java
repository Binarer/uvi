package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record PlaceDto(
        Long id,
        String name,
        String description,
        PlaceType type,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        String mainPhotoUrl,
        List<String> photos,
        String color,
        String websiteUrl,
        String phoneNumber,
        Boolean isActive,
        Long createdById,
        Set<TagDto> tags,
        LocalDateTime createdAt
) {}
