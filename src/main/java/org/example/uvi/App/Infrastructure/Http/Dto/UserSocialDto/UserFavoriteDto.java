package org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Information about a place added to user's favorites")
public record UserFavoriteDto(
    @Schema(description = "Unique identifier of the favorite record", example = "1")
    Long id,
    @Schema(description = "ID of the favorited place", example = "42")
    Long placeId,
    @Schema(description = "Name of the favorited place", example = "Dendrological Park")
    String placeName,
    @Schema(description = "Main photo URL of the favorited place", example = "https://example.com/photo.jpg")
    String placeMainPhotoUrl,
    @Schema(description = "Timestamp when it was added to favorites")
    LocalDateTime createdAt
) {}
