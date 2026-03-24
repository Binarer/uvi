package org.example.uvi.App.Infrastructure.Http.Controller.FamilyController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Services.FamilyService.FamilyService;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.AddFamilyMemberRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.CreateFamilyRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.FamilyDto;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.FamilyMemberDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.FamilyMapper.FamilyMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
@Tag(name = "Families", description = "Family and group management")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {

    private final FamilyService familyService;
    private final FamilyMapper familyMapper;

    @PostMapping
    @Operation(summary = "Create a new family", description = "Creates a new family/group and makes the current user the creator.")
    public ResponseEntity<FamilyDto> createFamily(
            Authentication auth,
            @Valid @RequestBody CreateFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.createFamily(userId, request.name(), request.description(), request.avatarUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(familyMapper.toDto(family));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get family by ID")
    public ResponseEntity<FamilyDto> getFamily(
            @Parameter(description = "ID of the family", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(familyMapper.toDto(familyService.getFamilyById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all active families", description = "Returns a list of all families with ACTIVE status.")
    public ResponseEntity<List<FamilyDto>> getAllFamilies() {
        return ResponseEntity.ok(familyService.getAllFamilies().stream()
                .map(familyMapper::toDto).toList());
    }

    @PostMapping("/leave")
    @Operation(summary = "Leave current family", description = "The current user leaves their active family. If the user is the creator, the family is deactivated.")
    public ResponseEntity<Void> leaveFamily(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyService.leaveFamily(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a family", description = "Marks a family as DEACTIVATED (soft delete).")
    public ResponseEntity<Void> deleteFamily(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        familyService.deleteFamily(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to family", description = "Adds an existing user to the family as a member.")
    public ResponseEntity<FamilyMemberDto> addMember(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        Long userId = (Long) auth.getPrincipal();
        FamilyMember member = familyService.addMember(id, request.userId(), request.role(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(familyMapper.toMemberDto(member));
    }

    @DeleteMapping("/{familyId}/members/{userId}")
    @Operation(summary = "Remove a member from family")
    public ResponseEntity<Void> removeMember(
            Authentication auth,
            @PathVariable Long familyId,
            @PathVariable Long userId) {
        Long requesterId = (Long) auth.getPrincipal();
        familyService.removeMember(familyId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get active members of a family")
    public ResponseEntity<List<FamilyMemberDto>> getMembers(
            @Parameter(description = "ID of the family", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(familyService.getFamilyMembers(id).stream()
                .map(familyMapper::toMemberDto).toList());
    }
}
