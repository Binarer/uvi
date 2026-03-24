package org.example.uvi.App.Infrastructure.Http.Mapper.UserLocationMapper;

import org.example.uvi.App.Domain.Models.UserLocation.UserLocation;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationDto;
import org.springframework.stereotype.Component;

@Component
public class UserLocationMapper {

    public UserLocationDto toDto(UserLocation location) {
        if (location == null) return null;
        double lat = location.getCoordinates().getY();
        double lon = location.getCoordinates().getX();
        return new UserLocationDto(
                location.getId(),
                location.getUser().getId(),
                lat,
                lon,
                location.getAccuracy(),
                location.getBatteryLevel(),
                location.getSpeed(),
                location.getTimestamp()
        );
    }
}
