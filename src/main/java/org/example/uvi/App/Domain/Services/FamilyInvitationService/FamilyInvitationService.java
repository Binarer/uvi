package org.example.uvi.App.Domain.Services.FamilyInvitationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;
import org.example.uvi.App.Domain.Enums.InvitationStatus.InvitationStatus;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyInvitation;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.FamilyInvitationRepository.FamilyInvitationRepository;
import org.example.uvi.App.Domain.Repository.FamilyMemberRepository.FamilyMemberRepository;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.example.uvi.App.Domain.Services.FamilyService.FamilyService;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyInvitationService {

    private final FamilyInvitationRepository invitationRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final FamilyService familyService;
    private final UserService userService;

    private static final int INVITATION_EXPIRY_HOURS = 48;

    @Transactional
    public FamilyInvitation createInvitation(Long familyId, Long inviterId,
                                              String inviteePhone, String message) {
        Family family = familyService.getFamilyById(familyId);
        User inviter = userService.getUserById(inviterId);

        Optional<User> invitee = userRepository.findByPhoneNumberActive(inviteePhone);

        if (invitee.isPresent()) {
            boolean alreadyMember = familyMemberRepository
                    .existsByFamilyAndUserAndIsActive(family, invitee.get(), true);
            if (alreadyMember) {
                throw new IllegalStateException("User is already a family member");
            }

            boolean alreadyInvited = invitationRepository.existsActiveInvitation(
                    familyId, invitee.get().getId(), LocalDateTime.now());
            if (alreadyInvited) {
                throw new IllegalStateException("User already has a pending invitation");
            }
        }

        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family)
                .inviter(inviter)
                .invitee(invitee.orElse(null))
                .inviteePhone(inviteePhone)
                .status(InvitationStatus.PENDING)
                .invitationCode(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(INVITATION_EXPIRY_HOURS))
                .message(message)
                .build();

        FamilyInvitation saved = invitationRepository.save(invitation);
        log.info("Invitation created: family={}, inviter={}, phone={}", familyId, inviterId, inviteePhone);
        return saved;
    }

    @Transactional
    public FamilyMember acceptInvitation(String invitationCode, Long userId) {
        FamilyInvitation invitation = getActiveInvitation(invitationCode);
        User user = userService.getUserById(userId);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitation.setInvitee(user);
        invitationRepository.save(invitation);

        FamilyMember member = FamilyMember.builder()
                .family(invitation.getFamily())
                .user(user)
                .role(FamilyMemberRole.MEMBER)
                .isActive(true)
                .build();

        FamilyMember savedMember = familyMemberRepository.save(member);
        log.info("Invitation {} accepted by user {}", invitationCode, userId);
        return savedMember;
    }

    @Transactional
    public void declineInvitation(String invitationCode, Long userId) {
        FamilyInvitation invitation = getActiveInvitation(invitationCode);

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("Invitation {} declined by user {}", invitationCode, userId);
    }

    @Transactional
    public void cancelInvitation(String invitationCode, Long requesterId) {
        FamilyInvitation invitation = invitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found: " + invitationCode));

        if (!invitation.getInviter().getId().equals(requesterId)) {
            throw new IllegalStateException("Only the inviter can cancel an invitation");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
        log.info("Invitation {} cancelled by user {}", invitationCode, requesterId);
    }

    public List<FamilyInvitation> getFamilyInvitations(Long familyId) {
        Family family = familyService.getFamilyById(familyId);
        return invitationRepository.findByFamily(family);
    }

    public List<FamilyInvitation> getUserPendingInvitations(Long userId) {
        User user = userService.getUserById(userId);
        return invitationRepository.findByInviteeAndStatus(user, InvitationStatus.PENDING);
    }

    @Transactional
    public void expireOldInvitations() {
        List<FamilyInvitation> expired = invitationRepository
                .findExpiredInvitations(LocalDateTime.now());
        expired.forEach(inv -> inv.setStatus(InvitationStatus.EXPIRED));
        invitationRepository.saveAll(expired);
        log.info("Expired {} invitations", expired.size());
    }

    private FamilyInvitation getActiveInvitation(String code) {
        FamilyInvitation invitation = invitationRepository.findByInvitationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found: " + code));

        if (!invitation.isActive()) {
            throw new IllegalStateException("Invitation is no longer active (status: " + invitation.getStatus() + ")");
        }

        return invitation;
    }
}
