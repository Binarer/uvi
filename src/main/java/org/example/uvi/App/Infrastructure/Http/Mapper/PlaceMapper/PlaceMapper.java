package org.example.uvi.App.Infrastructure.Http.Mapper.PlaceMapper;

import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto.PlaceDto;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto.TagDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.TagMapper.TagMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceMapper {

    private final TagMapper tagMapper;

    public PlaceDto toDto(Place place) {
        if (place == null) return null;
        Set<TagDto> tagDtos = place.getTags().stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toSet());

        return new PlaceDto(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getType(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getImageUrl(),
                place.getMainPhotoUrl(),
                place.getPhotos(),
                place.getColor(),
                place.getWebsiteUrl(),
                place.getPhoneNumber(),
                place.isActive(),
                place.getCreatedBy() != null ? place.getCreatedBy().getId() : null,
                tagDtos,
                place.getCreatedAt()
        );
    }
}
