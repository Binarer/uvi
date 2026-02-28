package org.example.uvi.App.Domain.Services.UserLocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Models.UserLocation.UserLocation;
import org.example.uvi.App.Domain.Repository.UserLocationRepository.UserLocationRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationService {

    private final UserLocationRepository userLocationRepository;
    private final UserService userService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public UserLocation saveLocation(Long userId, double latitude, double longitude,
                                     Float accuracy, Integer batteryLevel, Float speed) {
        User user = userService.getUserById(userId);
        Point coordinates = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        UserLocation location = UserLocation.builder()
                .user(user)
                .coordinates(coordinates)
                .accuracy(accuracy)
                .batteryLevel(batteryLevel)
                .speed(speed)
                .timestamp(Instant.now())
                .build();

        return userLocationRepository.save(location);
    }

    public Optional<UserLocation> getLatestLocation(Long userId) {
        return userLocationRepository.findTopByUserIdOrderByTimestampDesc(userId);
    }

    public List<UserLocation> getLocationHistory(Long userId) {
        return userLocationRepository.findAllByUserIdOrderByTimestampDesc(userId);
    }

    public List<UserLocation> getLocationHistory(Long userId, Instant from, Instant to) {
        return userLocationRepository.findByUserIdAndTimeRange(userId, from, to);
    }

    public List<UserLocation> getNearbyUsers(double latitude, double longitude, double radiusMeters) {
        return userLocationRepository.findUsersNearLocation(latitude, longitude, radiusMeters);
    }
}
