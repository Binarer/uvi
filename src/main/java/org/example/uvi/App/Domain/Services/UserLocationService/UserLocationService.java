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

    private static final double MAX_ACCURACY_METERS = 50.0;
    private static final double MIN_DISTANCE_METERS = 5.0;

    @Transactional
    public UserLocation saveLocation(Long userId, double latitude, double longitude,
                                     Float accuracy, Integer batteryLevel, Float speed) {
        User user = userService.getUserById(userId);
        
        // Check GPS accuracy
        if (accuracy != null && accuracy > MAX_ACCURACY_METERS) {
            log.warn("Location accuracy too low: {}m (max: {}m)", accuracy, MAX_ACCURACY_METERS);
            throw new IllegalArgumentException("Location accuracy too low: " + accuracy + "m (max: " + MAX_ACCURACY_METERS + "m)");
        }
        
        // Check if distance moved is significant
        var latestLocation = userLocationRepository.findTopByUserIdOrderByTimestampDesc(userId);
        if (latestLocation.isPresent()) {
            double lastLat = latestLocation.get().getCoordinates().getY();
            double lastLon = latestLocation.get().getCoordinates().getX();
            double distance = haversineDistance(lastLat, lastLon, latitude, longitude);
            
            if (distance < MIN_DISTANCE_METERS) {
                log.debug("Location update skipped: distance {} m < {} m threshold", distance, MIN_DISTANCE_METERS);
                return latestLocation.get();
            }
        }
        
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

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of first point (degrees)
     * @param lon1 Longitude of first point (degrees)
     * @param lat2 Latitude of second point (degrees)
     * @param lon2 Longitude of second point (degrees)
     * @return Distance in meters
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_METERS = 6371000;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
