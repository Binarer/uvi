package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.*;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;

public record CreatePlaceRequest(
        @NotBlank(message = "Place name is required")
        @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
        String name,

        String description,

        @NotNull(message = "Place type is required")
        PlaceType type,

        @Size(max = 500)
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double longitude,

        String imageUrl,
        String websiteUrl,

        @Pattern(regexp = "^\\+?[0-9\\s\\-().]{7,25}$", message = "Invalid phone number format")
        String phoneNumber
) {}
