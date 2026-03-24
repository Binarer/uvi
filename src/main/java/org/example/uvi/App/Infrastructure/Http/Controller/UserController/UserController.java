package org.example.uvi.App.Infrastructure.Http.Controller.UserController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserDto.UpdateUserRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.UserDto.UserDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserMapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and account management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently authenticated user.")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(userMapper.toDto(userService.getUserById(userId)));
    }

    @PutMapping("/me")
    @Operation(
            summary = "Update current user profile",
            description = "Allows the user to update their personal details like name, username, and city."
    )
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
    @Operation(
            summary = "Delete current user account",
            description = "Marks the current user account as DELETED. This is a soft delete.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account successfully deactivated")
            }
    )
    public ResponseEntity<Void> deleteCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toDto(userService.getUserById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all active users", description = "Returns a list of all users with ACTIVE status.")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(userMapper::toDto).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name", description = "Fuzzy search by first name or last name.")
    public ResponseEntity<List<UserDto>> searchUsers(
            @Parameter(description = "Search query (part of name)", example = "Иван")
            @RequestParam String name) {
        return ResponseEntity.ok(userService.searchByName(name).stream()
                .map(userMapper::toDto).toList());
    }
}
