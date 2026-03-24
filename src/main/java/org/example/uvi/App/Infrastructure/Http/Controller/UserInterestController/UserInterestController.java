package org.example.uvi.App.Infrastructure.Http.Controller.UserInterestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Services.UserInterestService.UserInterestService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto.AddUserInterestRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto.SetUserInterestsRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto.UserInterestDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserInterestMapper.UserInterestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interests")
@RequiredArgsConstructor
@Tag(name = "User Interests", description = "Management of user interests and preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserInterestController {

    private final UserInterestService userInterestService;
    private final UserInterestMapper userInterestMapper;

    @GetMapping
    @Operation(summary = "Get current user's interests", description = "Returns a list of interests ordered by preference level.")
    public ResponseEntity<List<UserInterestDto>> getMyInterests(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(userInterestService.getUserInterests(userId).stream()
                .map(userInterestMapper::toDto).toList());
    }

    @PostMapping
    @Operation(summary = "Add an interest", description = "Adds a new interest to the user's profile with a specified preference level.")
    public ResponseEntity<UserInterestDto> addInterest(
            Authentication auth,
            @Valid @RequestBody AddUserInterestRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userInterestMapper.toDto(
                        userInterestService.addInterest(userId, request.interest(), request.preferenceLevel())));
    }

    @PutMapping
    @Operation(summary = "Replace all interests", description = "Removes all existing interests and sets the provided ones.")
    public ResponseEntity<Void> setInterests(
            Authentication auth,
            @Valid @RequestBody SetUserInterestsRequest request) {
        Long userId = (Long) auth.getPrincipal();
        userInterestService.setInterests(userId, request.interests());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{interest}")
    @Operation(summary = "Remove an interest")
    public ResponseEntity<Void> removeInterest(
            Authentication auth,
            @Parameter(description = "Interest enum value", example = "SPORTS")
            @PathVariable Interest interest) {
        Long userId = (Long) auth.getPrincipal();
        userInterestService.removeInterest(userId, interest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{interest}/increase")
    @Operation(summary = "Increase preference level", description = "Increases the preference level of a specific interest by 1.")
    public ResponseEntity<UserInterestDto> increasePreference(
            Authentication auth,
            @Parameter(description = "Interest enum value", example = "SPORTS")
            @PathVariable Interest interest) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(
                userInterestMapper.toDto(userInterestService.increasePreference(userId, interest)));
    }

    @PostMapping("/{interest}/decrease")
    @Operation(summary = "Decrease preference level", description = "Decreases the preference level of a specific interest by 1.")
    public ResponseEntity<UserInterestDto> decreasePreference(
            Authentication auth,
            @Parameter(description = "Interest enum value", example = "SPORTS")
            @PathVariable Interest interest) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(
                userInterestMapper.toDto(userInterestService.decreasePreference(userId, interest)));
    }
}
