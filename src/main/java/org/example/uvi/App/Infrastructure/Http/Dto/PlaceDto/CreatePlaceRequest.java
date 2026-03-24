package org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;

import java.util.List;

@Schema(description = "Request to create a new place")
public record CreatePlaceRequest(
        @NotBlank(message = "Place name is required")
        @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
        @Schema(description = "Name of the place", example = "Dendrological Park")
        String name,

        @Schema(description = "Detailed description", example = "A beautiful park in the center of Yekaterinburg")
        String description,

        @NotNull(message = "Place type is required")
        @Schema(description = "Type of the place", example = "PARK")
        PlaceType type,

        @Size(max = 500)
        @Schema(description = "Physical address", example = "8 Marta St, 37")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        @Schema(description = "Latitude coordinate", example = "56.8286")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        @Schema(description = "Longitude coordinate", example = "60.6033")
        Double longitude,

        @Schema(description = "Icon or small image URL", example = "https://example.com/icon.png")
        String imageUrl,

        @Schema(description = "Main large photo URL", example = "https://example.com/main.jpg")
        String mainPhotoUrl,

        @Schema(description = "Gallery of additional photos")
        List<String> photos,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
        @Schema(description = "Hex color code for UI", example = "#FF5733")
        String color,

        @Schema(description = "Official website URL", example = "https://park-ekb.ru")
        String websiteUrl,

        @Pattern(regexp = "^\\+?[0-9\\s\\-().]{7,25}$", message = "Invalid phone number format")
        @Schema(description = "Contact phone number", example = "+73431234567")
        String phoneNumber
) {}
