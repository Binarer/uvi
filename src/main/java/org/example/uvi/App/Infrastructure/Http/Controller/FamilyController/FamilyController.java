package org.example.uvi.App.Infrastructure.Http.Controller.FamilyController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Services.FamilyService.FamilyService;
import org.example.uvi.App.Infrastructure.Http.Dto.*;
import org.example.uvi.App.Infrastructure.Http.Mapper.FamilyMapper.FamilyMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
@Tag(name = "Families", description = "Family management")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {

    private final FamilyService familyService;
    private final FamilyMapper familyMapper;

    @PostMapping
    @Operation(summary = "Create a new family")
    public ResponseEntity<FamilyDto> createFamily(
            Authentication auth,
            @Valid @RequestBody CreateFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.createFamily(userId, request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(familyMapper.toDto(family));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get family by ID")
    public ResponseEntity<FamilyDto> getFamily(@PathVariable Long id) {
        return ResponseEntity.ok(familyMapper.toDto(familyService.getFamilyById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all active families")
    public ResponseEntity<List<FamilyDto>> getAllFamilies() {
        return ResponseEntity.ok(familyService.getAllFamilies().stream()
                .map(familyMapper::toDto).toList());
    }

    @GetMapping("/my")
    @Operation(summary = "Get families where current user is a member")
    public ResponseEntity<List<FamilyDto>> getMyFamilies(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(familyService.getFamiliesByUser(userId).stream()
                .map(familyMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update family (admin only)")
    public ResponseEntity<FamilyDto> updateFamily(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody CreateFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.updateFamily(id, userId, request.name(), request.description(), null);
        return ResponseEntity.ok(familyMapper.toDto(family));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete family (admin only)")
    public ResponseEntity<Void> deleteFamily(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        familyService.deleteFamily(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get active members of a family")
    public ResponseEntity<List<FamilyMemberDto>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(familyService.getFamilyMembers(id).stream()
                .map(familyMapper::toMemberDto).toList());
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to family (admin only)")
    public ResponseEntity<FamilyMemberDto> addMember(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        Long userId = (Long) auth.getPrincipal();
        FamilyMember member = familyService.addMember(id, request.userId(), request.role(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(familyMapper.toMemberDto(member));
    }

    @DeleteMapping("/{familyId}/members/{memberId}")
    @Operation(summary = "Remove member from family (admin only)")
    public ResponseEntity<Void> removeMember(
            Authentication auth,
            @PathVariable Long familyId,
            @PathVariable Long memberId) {
        Long userId = (Long) auth.getPrincipal();
        familyService.removeMember(familyId, memberId, userId);
        return ResponseEntity.noContent().build();
    }
}
