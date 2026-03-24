package org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto.TagDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "Information about a place (POI)")
public record PlaceDto(
        @Schema(description = "Unique identifier", example = "1")
        Long id,
        
        @Schema(description = "Name of the place", example = "Dendrological Park")
        String name,
        
        @Schema(description = "Detailed description", example = "A beautiful park in the center of Yekaterinburg")
        String description,
        
        @Schema(description = "Type of the place", example = "PARK")
        PlaceType type,
        
        @Schema(description = "Physical address", example = "8 Marta St, 37")
        String address,
        
        @Schema(description = "Latitude coordinate", example = "56.8286")
        Double latitude,
        
        @Schema(description = "Longitude coordinate", example = "60.6033")
        Double longitude,
        
        @Schema(description = "Icon or small image URL", example = "https://example.com/icon.png")
        String imageUrl,
        
        @Schema(description = "Main large photo URL", example = "https://example.com/main.jpg")
        String mainPhotoUrl,
        
        @Schema(description = "Gallery of additional photos")
        List<String> photos,
        
        @Schema(description = "Hex color code for UI", example = "#FF5733")
        String color,
        
        @Schema(description = "Official website URL", example = "https://park-ekb.ru")
        String websiteUrl,
        
        @Schema(description = "Contact phone number", example = "+73431234567")
        String phoneNumber,
        
        @Schema(description = "Is the place currently active")
        boolean isActive,
        
        @Schema(description = "ID of the user who created this place")
        Long createdById,
        
        @Schema(description = "Set of tags associated with the place")
        Set<TagDto> tags,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
