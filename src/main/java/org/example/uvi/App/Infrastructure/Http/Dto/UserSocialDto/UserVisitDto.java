package org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto;

import java.time.LocalDateTime;

public record UserVisitDto(
    Long id,
    Long placeId,
    String placeName,
    String placeMainPhotoUrl,
    LocalDateTime visitedAt,
    String comment,
    Integer rating
) {}
