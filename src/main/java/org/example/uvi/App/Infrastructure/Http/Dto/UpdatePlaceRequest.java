package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;

import java.util.List;

public record UpdatePlaceRequest(
        @Size(min = 1, max = 200)
        String name,

        String description,
        PlaceType type,

        @Size(max = 500)
        String address,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        Double latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        Double longitude,

        String imageUrl,
        String mainPhotoUrl,
        List<String> photos,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
        String color,

        String websiteUrl,
        String phoneNumber
) {}
