package org.example.uvi.App.Infrastructure.Http.Controller.DeviceController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.DeviceService.DeviceService;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceRequest;
import org.example.uvi.App.Infrastructure.Http.Mapper.DeviceMapper.DeviceMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Device token management for push notifications")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    @Operation(summary = "Register a device for push notifications")
    public ResponseEntity<DeviceDto> registerDevice(
            Authentication auth,
            @Valid @RequestBody DeviceRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                deviceMapper.toDto(deviceService.registerDevice(userId, request.deviceToken(), request.osType())));
    }

    @GetMapping
    @Operation(summary = "Get all registered devices for current user")
    public ResponseEntity<List<DeviceDto>> getMyDevices(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(deviceService.getDevicesByUser(userId).stream()
                .map(deviceMapper::toDto).toList());
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Unregister a device")
    public ResponseEntity<Void> deleteDevice(
            Authentication auth,
            @PathVariable UUID deviceId) {
        Long userId = (Long) auth.getPrincipal();
        deviceService.deleteDevice(userId, deviceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Unregister all devices for current user")
    public ResponseEntity<Void> deleteAllDevices(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        deviceService.deleteAllUserDevices(userId);
        return ResponseEntity.noContent().build();
    }
}
