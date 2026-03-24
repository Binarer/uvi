package org.example.uvi.App.Infrastructure.Http.Controller.UserLocationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.UserLocationService.UserLocationService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationDto;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationRequest;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserLocationMapper.UserLocationMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "User Locations", description = "Real-time location tracking")
@SecurityRequirement(name = "bearerAuth")
public class UserLocationController {

    private final UserLocationService userLocationService;
    private final UserLocationMapper userLocationMapper;

    @PostMapping
    @Operation(
            summary = "Save current user location",
            description = "Saves a new location point for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Location successfully saved"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<UserLocationDto> saveLocation(
            Authentication auth,
            @Valid @RequestBody UserLocationRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userLocationMapper.toDto(userLocationService.saveLocation(
                        userId, request.latitude(), request.longitude(),
                        request.accuracy(), request.batteryLevel(), request.speed())));
    }

    @GetMapping("/me/latest")
    @Operation(
            summary = "Get latest location of current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Latest location found"),
                    @ApiResponse(responseCode = "404", description = "No location data found for user")
            }
    )
    public ResponseEntity<UserLocationDto> getMyLatestLocation(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userLocationService.getLatestLocation(userId)
                .map(loc -> ResponseEntity.ok(userLocationMapper.toDto(loc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/history")
    @Operation(
            summary = "Get location history of current user",
            description = "Returns a list of previous locations for the authenticated user, optionally filtered by time range."
    )
    public ResponseEntity<List<UserLocationDto>> getMyHistory(
            Authentication auth,
            @Parameter(description = "Start time (ISO 8601)", example = "2024-03-24T10:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End time (ISO 8601)", example = "2024-03-24T18:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Long userId = (Long) auth.getPrincipal();
        List<UserLocationDto> history = (from != null && to != null)
                ? userLocationService.getLocationHistory(userId, from, to).stream()
                        .map(userLocationMapper::toDto).toList()
                : userLocationService.getLocationHistory(userId).stream()
                        .map(userLocationMapper::toDto).toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/nearby")
    @Operation(
            summary = "Find users near a given location",
            description = "Returns a list of other users' latest locations within the specified radius."
    )
    public ResponseEntity<List<UserLocationDto>> getNearbyUsers(
            @Parameter(description = "Center latitude", example = "56.8286")
            @RequestParam double latitude,
            @Parameter(description = "Center longitude", example = "60.6033")
            @RequestParam double longitude,
            @Parameter(description = "Search radius in meters", example = "500")
            @RequestParam(defaultValue = "1000") double radiusMeters) {
        return ResponseEntity.ok(
                userLocationService.getNearbyUsers(latitude, longitude, radiusMeters).stream()
                        .map(userLocationMapper::toDto).toList());
    }
}
