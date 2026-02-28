package org.example.uvi.App.Domain.Services.PlaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.PlaceRepository.PlaceRepository;
import org.example.uvi.App.Domain.Repository.TagRepository.TagRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Cacheable(value = "places", key = "#id")
    public Place getPlaceById(Long id) {
        return placeRepository.findById(id)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + id));
    }

    public List<Place> getAllPlaces() {
        return placeRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<Place> getPlacesByType(PlaceType type) {
        return placeRepository.findByTypeAndIsActiveTrue(type);
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public Place createPlace(Long creatorId, String name, String description,
                             PlaceType type, String address,
                             double latitude, double longitude,
                             String imageUrl, String websiteUrl, String phoneNumber) {
        User creator = userService.getUserById(creatorId);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Place place = Place.builder()
                .name(name)
                .description(description)
                .type(type)
                .address(address)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .createdBy(creator)
                .imageUrl(imageUrl)
                .websiteUrl(websiteUrl)
                .phoneNumber(phoneNumber)
                .isActive(true)
                .build();

        return placeRepository.save(place);
    }

    @Transactional
    @CacheEvict(value = "places", key = "#id")
    public Place updatePlace(Long id, String name, String description,
                             PlaceType type, String address,
                             Double latitude, Double longitude,
                             String imageUrl, String websiteUrl, String phoneNumber) {
        Place place = getPlaceById(id);

        if (name != null) place.setName(name);
        if (description != null) place.setDescription(description);
        if (type != null) place.setType(type);
        if (address != null) place.setAddress(address);
        if (imageUrl != null) place.setImageUrl(imageUrl);
        if (websiteUrl != null) place.setWebsiteUrl(websiteUrl);
        if (phoneNumber != null) place.setPhoneNumber(phoneNumber);

        if (latitude != null && longitude != null) {
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            place.setLocation(location);
            place.setLatitude(latitude);
            place.setLongitude(longitude);
        }

        return placeRepository.save(place);
    }

    @Transactional
    @CacheEvict(value = "places", key = "#id")
    public void deletePlace(Long id) {
        Place place = getPlaceById(id);
        place.setIsActive(false);
        placeRepository.save(place);
        log.info("Place {} deactivated", id);
    }

    public List<Place> findNearby(double latitude, double longitude, double radiusMeters) {
        return placeRepository.findNearbyPlaces(latitude, longitude, radiusMeters);
    }

    public List<Place> findNearbyByType(double latitude, double longitude,
                                        double radiusMeters, PlaceType type) {
        return placeRepository.findNearbyPlacesByType(latitude, longitude, radiusMeters, type.name());
    }

    public List<Place> getRecommendedPlaces(Long userId, double latitude,
                                            double longitude, double radiusMeters, int limit) {
        Set<Interest> interests = userService.getUserById(userId)
                .getUserInterests().stream()
                .map(ui -> ui.getInterest())
                .collect(Collectors.toSet());

        if (interests.isEmpty()) {
            return placeRepository.findNearestPlaces(latitude, longitude, limit);
        }

        List<String> interestNames = interests.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return placeRepository.findRecommendedPlaces(interestNames, latitude, longitude, radiusMeters, limit);
    }

    @Transactional
    @CacheEvict(value = "places", key = "#placeId")
    public Place addTagsToPlace(Long placeId, Set<Long> tagIds) {
        Place place = getPlaceById(placeId);
        List<Tag> tags = tagRepository.findAllById(tagIds);
        tags.forEach(place::addTag);
        return placeRepository.save(place);
    }

    @Transactional
    @CacheEvict(value = "places", key = "#placeId")
    public Place removeTagFromPlace(Long placeId, Long tagId) {
        Place place = getPlaceById(placeId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        place.removeTag(tag);
        return placeRepository.save(place);
    }

    public List<Place> findByInterests(List<String> interests) {
        return placeRepository.findByInterests(interests);
    }

    public List<Place> findByInterestsNearby(List<String> interests,
                                              double latitude, double longitude, double radiusMeters) {
        return placeRepository.findByInterestsNearby(interests, latitude, longitude, radiusMeters);
    }
}
