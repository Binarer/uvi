package org.example.uvi.App.Infrastructure.Http.Controller.UserLocationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.UserLocationService.UserLocationService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationRequest;
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
    @Operation(summary = "Save current user location")
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
    @Operation(summary = "Get latest location of current user")
    public ResponseEntity<UserLocationDto> getMyLatestLocation(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userLocationService.getLatestLocation(userId)
                .map(loc -> ResponseEntity.ok(userLocationMapper.toDto(loc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/history")
    @Operation(summary = "Get location history of current user")
    public ResponseEntity<List<UserLocationDto>> getMyHistory(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
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
    @Operation(summary = "Find users near a given location")
    public ResponseEntity<List<UserLocationDto>> getNearbyUsers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") double radiusMeters) {
        return ResponseEntity.ok(
                userLocationService.getNearbyUsers(latitude, longitude, radiusMeters).stream()
                        .map(userLocationMapper::toDto).toList());
    }
}
