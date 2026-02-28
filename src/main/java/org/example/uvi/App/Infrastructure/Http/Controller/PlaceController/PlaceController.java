package org.example.uvi.App.Infrastructure.Http.Controller.PlaceController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Services.PlaceService.PlaceService;
import org.example.uvi.App.Infrastructure.Http.Dto.*;
import org.example.uvi.App.Infrastructure.Http.Mapper.PlaceMapper.PlaceMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "Place management with geolocation")
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    @PostMapping
    @Operation(summary = "Create a new place")
    public ResponseEntity<PlaceDto> createPlace(
            Authentication auth,
            @Valid @RequestBody CreatePlaceRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Place place = placeService.createPlace(
                userId, request.name(), request.description(), request.type(),
                request.address(), request.latitude(), request.longitude(),
                request.imageUrl(), request.websiteUrl(), request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(placeMapper.toDto(place));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get place by ID")
    public ResponseEntity<PlaceDto> getPlace(@PathVariable Long id) {
        return ResponseEntity.ok(placeMapper.toDto(placeService.getPlaceById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all active places")
    public ResponseEntity<List<PlaceDto>> getAllPlaces(
            @RequestParam(required = false) PlaceType type) {
        List<Place> places = type != null
                ? placeService.getPlacesByType(type)
                : placeService.getAllPlaces();
        return ResponseEntity.ok(places.stream().map(placeMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a place")
    public ResponseEntity<PlaceDto> updatePlace(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlaceRequest request) {
        Place place = placeService.updatePlace(
                id, request.name(), request.description(), request.type(),
                request.address(), request.latitude(), request.longitude(),
                request.imageUrl(), request.websiteUrl(), request.phoneNumber());
        return ResponseEntity.ok(placeMapper.toDto(place));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a place")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby places by coordinates")
    public ResponseEntity<List<PlaceDto>> findNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") double radiusMeters,
            @RequestParam(required = false) PlaceType type) {
        List<Place> places = type != null
                ? placeService.findNearbyByType(latitude, longitude, radiusMeters, type)
                : placeService.findNearby(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(places.stream().map(placeMapper::toDto).toList());
    }

    @GetMapping("/recommended")
    @Operation(summary = "Get personalized place recommendations")
    public ResponseEntity<List<PlaceDto>> getRecommended(
            Authentication auth,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radiusMeters,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) auth.getPrincipal();
        List<Place> places = placeService.getRecommendedPlaces(userId, latitude, longitude, radiusMeters, limit);
        return ResponseEntity.ok(places.stream().map(placeMapper::toDto).toList());
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Add tags to a place")
    public ResponseEntity<PlaceDto> addTags(
            @PathVariable Long id,
            @Valid @RequestBody AddPlaceTagsRequest request) {
        Place place = placeService.addTagsToPlace(id, request.tagIds());
        return ResponseEntity.ok(placeMapper.toDto(place));
    }

    @DeleteMapping("/{placeId}/tags/{tagId}")
    @Operation(summary = "Remove a tag from a place")
    public ResponseEntity<PlaceDto> removeTag(
            @PathVariable Long placeId,
            @PathVariable Long tagId) {
        Place place = placeService.removeTagFromPlace(placeId, tagId);
        return ResponseEntity.ok(placeMapper.toDto(place));
    }
}
