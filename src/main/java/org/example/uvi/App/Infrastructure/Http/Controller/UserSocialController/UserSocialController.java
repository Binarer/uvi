package org.example.uvi.App.Infrastructure.Http.Controller.UserSocialController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.UserSocialService.UserSocialService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto.UserFavoriteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto.UserVisitDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserSocialMapper.UserSocialMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/social")
@RequiredArgsConstructor
@Tag(name = "User Social", description = "User favorites and visit history")
@SecurityRequirement(name = "bearerAuth")
public class UserSocialController {

    private final UserSocialService socialService;
    private final UserSocialMapper socialMapper;

    @PostMapping("/favorites/{placeId}")
    @Operation(
            summary = "Add place to favorites",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Place added to favorites"),
                    @ApiResponse(responseCode = "404", description = "Place not found")
            }
    )
    public ResponseEntity<Void> addToFavorites(
            Authentication auth,
            @Parameter(description = "ID of the place", example = "42")
            @PathVariable Long placeId) {
        Long userId = (Long) auth.getPrincipal();
        socialService.addToFavorites(userId, placeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/favorites/{placeId}")
    @Operation(
            summary = "Remove place from favorites",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Place removed from favorites"),
                    @ApiResponse(responseCode = "404", description = "Favorite record not found")
            }
    )
    public ResponseEntity<Void> removeFromFavorites(
            Authentication auth,
            @Parameter(description = "ID of the place", example = "42")
            @PathVariable Long placeId) {
        Long userId = (Long) auth.getPrincipal();
        socialService.removeFromFavorites(userId, placeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get current user's favorite places")
    public ResponseEntity<List<UserFavoriteDto>> getFavorites(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(socialService.getUserFavorites(userId).stream()
                .map(socialMapper::toFavoriteDto).toList());
    }

    @PostMapping("/visits/{placeId}")
    @Operation(
            summary = "Record a visit to a place",
            description = "Creates a record in the user's visit history with optional comment and rating."
    )
    public ResponseEntity<UserVisitDto> recordVisit(
            Authentication auth,
            @Parameter(description = "ID of the place", example = "42")
            @PathVariable Long placeId,
            @Parameter(description = "User comment about the visit", example = "Great place!")
            @RequestParam(required = false) String comment,
            @Parameter(description = "Rating (1-5)", example = "5")
            @RequestParam(required = false) Integer rating) {
        Long userId = (Long) auth.getPrincipal();
        var visit = socialService.addVisit(userId, placeId, comment, rating);
        return ResponseEntity.status(HttpStatus.CREATED).body(socialMapper.toVisitDto(visit));
    }

    @GetMapping("/visits")
    @Operation(summary = "Get current user's visit history")
    public ResponseEntity<List<UserVisitDto>> getVisits(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(socialService.getUserVisits(userId).stream()
                .map(socialMapper::toVisitDto).toList());
    }
}
