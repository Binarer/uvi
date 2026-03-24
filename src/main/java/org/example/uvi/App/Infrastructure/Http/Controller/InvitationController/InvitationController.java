package org.example.uvi.App.Infrastructure.Http.Controller.InvitationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.FamilyInvitationService.FamilyInvitationService;
import org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto.CreateInvitationRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto.InvitationDto;
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
    @Operation(summary = "Send an invitation to join a family")
    public ResponseEntity<InvitationDto> sendInvitation(
            Authentication auth,
            @PathVariable Long familyId,
            @Valid @RequestBody CreateInvitationRequest request) {
        Long inviterId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                invitationMapper.toDto(invitationService.createInvitation(
                        familyId, inviterId, request.inviteePhone(), request.message())));
    }

    @PostMapping("/{code}/accept")
    @Operation(summary = "Accept a family invitation")
    @ApiResponse(responseCode = "204", description = "Invitation accepted successfully")
    public ResponseEntity<Void> acceptInvitation(
            Authentication auth,
            @Parameter(description = "Invitation code", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String code) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.acceptInvitation(code, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{code}/reject")
    @Operation(summary = "Reject a family invitation")
    @ApiResponse(responseCode = "204", description = "Invitation rejected successfully")
    public ResponseEntity<Void> rejectInvitation(
            Authentication auth,
            @Parameter(description = "Invitation code", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String code) {
        Long userId = (Long) auth.getPrincipal();
        invitationService.declineInvitation(code, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/pending")
    @Operation(summary = "Get pending invitations for current user")
    @ApiResponse(responseCode = "200", description = "Pending invitations retrieved successfully")
    public ResponseEntity<List<InvitationDto>> getMyPendingInvitations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(invitationService.getUserPendingInvitations(userId).stream()
                .map(invitationMapper::toDto).toList());
    }

    @GetMapping("/families/{familyId}")
    @Operation(summary = "Get all invitations for a family")
    public ResponseEntity<List<InvitationDto>> getFamilyInvitations(
            @Parameter(description = "ID of the family", example = "10")
            @PathVariable Long familyId) {
        return ResponseEntity.ok(invitationService.getFamilyInvitations(familyId).stream()
                .map(invitationMapper::toDto).toList());
    }
}
