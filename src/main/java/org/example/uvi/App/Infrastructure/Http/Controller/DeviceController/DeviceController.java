package org.example.uvi.App.Infrastructure.Http.Controller.DeviceController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.DeviceService.DeviceService;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto.DeviceDto;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto.RegisterDeviceRequest;
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
    @Operation(
            summary = "Register or update a device",
            description = "Registers a new device token or updates an existing one for the current user."
    )
    public ResponseEntity<DeviceDto> registerDevice(
            Authentication auth,
            @Valid @RequestBody RegisterDeviceRequest request) {
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

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Unregister a device",
            description = "Deletes a device registration by its record ID."
    )
    @ApiResponse(responseCode = "204", description = "Device unregistered successfully")
    public ResponseEntity<Void> unregisterDevice(
            Authentication auth,
            @Parameter(description = "ID of the device record", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        Long userId = (Long) auth.getPrincipal();
        deviceService.deleteDevice(userId, id);
        return ResponseEntity.noContent().build();
    }
}
