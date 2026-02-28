package org.example.uvi.App.Infrastructure.Http.Controller.UserController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.example.uvi.App.Infrastructure.Http.Dto.UpdateUserRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.UserDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserMapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(userMapper.toDto(userService.getUserById(userId)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto> updateCurrentUser(
            Authentication auth,
            @Valid @RequestBody UpdateUserRequest request) {
        Long userId = (Long) auth.getPrincipal();
        User updated = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .dateOfBirth(request.dateOfBirth())
                .city(request.city())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        return ResponseEntity.ok(userMapper.toDto(userService.updateUser(userId, updated)));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete (soft) current user account")
    public ResponseEntity<Void> deleteCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toDto(userService.getUserById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all active users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(userMapper::toDto).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String name) {
        return ResponseEntity.ok(userService.searchByName(name).stream()
                .map(userMapper::toDto).toList());
    }
}
