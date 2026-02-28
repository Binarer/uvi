package org.example.uvi.App.Infrastructure.Http.Controller.InvitationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Models.Family.FamilyInvitation;
import org.example.uvi.App.Domain.Services.FamilyInvitationService.FamilyInvitationService;
import org.example.uvi.App.Infrastructure.Http.Dto.CreateInvitationRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.InvitationMapper.InvitationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Family invitation management")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final FamilyInvitationService invitationService;
    private final InvitationMapper invitationMapper;

    @PostMapping("/families/{familyId}")
    @Operation(summary = "Create an invitation for a family")
    public ResponseEntity<InvitationDto> createInvitation(
            Authentication auth,
            @PathVariable Long familyId,
            @Valid @RequestBody CreateInvitationRequest request) {
        Long userId = (Long) auth.getPrincipal();
        FamilyInvitation invitation = invitationService.createInvitation(
                familyId, userId, request.inviteePhone(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationMapper.toDto(invitation));
    }

    @PostMapping("/{code}/accept")
    @Operation(summary = "Accept a family invitation")
    public ResponseEntity<Void> acceptInvitation(
            Authentication auth,
            @PathVariable String code) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.acceptInvitation(code, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{code}/decline")
    @Operation(summary = "Decline a family invitation")
    public ResponseEntity<Void> declineInvitation(
            Authentication auth,
            @PathVariable String code) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.declineInvitation(code, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Cancel a sent invitation (inviter only)")
    public ResponseEntity<Void> cancelInvitation(
            Authentication auth,
            @PathVariable String code) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.cancelInvitation(code, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's pending invitations")
    public ResponseEntity<List<InvitationDto>> getMyInvitations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(invitationService.getUserPendingInvitations(userId).stream()
                .map(invitationMapper::toDto).toList());
    }

    @GetMapping("/families/{familyId}")
    @Operation(summary = "Get all invitations for a family")
    public ResponseEntity<List<InvitationDto>> getFamilyInvitations(@PathVariable Long familyId) {
        return ResponseEntity.ok(invitationService.getFamilyInvitations(familyId).stream()
                .map(invitationMapper::toDto).toList());
    }
}
