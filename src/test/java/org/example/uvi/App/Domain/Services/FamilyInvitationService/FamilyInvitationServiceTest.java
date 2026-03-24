package org.example.uvi.App.Domain.Services.FamilyInvitationService;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyInvitationServiceTest {

    @Mock private FamilyInvitationRepository invitationRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private FamilyService familyService;
    @Mock private UserService userService;

    @InjectMocks private FamilyInvitationService invitationService;

    private User inviter;
    private User invitee;
    private Family family;
    private FamilyInvitation pendingInvitation;

    @BeforeEach
    void setUp() {
        inviter = User.builder().id(1L).firstName("Inviter").phoneNumber("79001111111").build();
        invitee = User.builder().id(2L).firstName("Invitee").phoneNumber("79002222222").build();

        family = Family.builder()
                .id(1L).name("Test Family").creator(inviter)
                .members(new ArrayList<>()).invitations(new ArrayList<>())
                .maxMembers(10).build();

        pendingInvitation = FamilyInvitation.builder()
                .id(1L).family(family).inviter(inviter).invitee(invitee)
                .inviteePhone("79002222222")
                .status(InvitationStatus.PENDING)
                .invitationCode(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();
    }

    @Test
    void createInvitation_WhenValid_ReturnsInvitation() {
        when(familyService.getFamilyById(1L)).thenReturn(family);
        when(userService.getUserById(1L)).thenReturn(inviter);
        when(userRepository.findByPhoneNumberActive("79002222222")).thenReturn(Optional.of(invitee));
        when(familyMemberRepository.existsByUserAndIsActive(invitee, true)).thenReturn(false);
        when(invitationRepository.existsActiveInvitation(eq(1L), eq(2L), any())).thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FamilyInvitation result = invitationService.createInvitation(1L, 1L, "79002222222", "Join us!");

        assertNotNull(result);
        assertEquals(InvitationStatus.PENDING, result.getStatus());
        verify(invitationRepository).save(any());
    }

    @Test
    void createInvitation_WhenAlreadyMember_ThrowsException() {
        when(familyService.getFamilyById(1L)).thenReturn(family);
        when(userService.getUserById(1L)).thenReturn(inviter);
        when(userRepository.findByPhoneNumberActive("79002222222")).thenReturn(Optional.of(invitee));
        when(familyMemberRepository.existsByUserAndIsActive(invitee, true)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> invitationService.createInvitation(1L, 1L, "79002222222", null));
    }

    @Test
    void acceptInvitation_WhenPending_AcceptsAndCreatesMember() {
        when(invitationRepository.findByInvitationCode(pendingInvitation.getInvitationCode()))
                .thenReturn(Optional.of(pendingInvitation));
        when(userService.getUserById(2L)).thenReturn(invitee);
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(familyMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FamilyMember result = invitationService.acceptInvitation(
                pendingInvitation.getInvitationCode(), 2L);

        assertNotNull(result);
        assertEquals(InvitationStatus.ACCEPTED, pendingInvitation.getStatus());
        assertEquals(FamilyMemberRole.MEMBER, result.getRole());
    }

    @Test
    void declineInvitation_WhenPending_DeclinesInvitation() {
        when(invitationRepository.findByInvitationCode(pendingInvitation.getInvitationCode()))
                .thenReturn(Optional.of(pendingInvitation));
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invitationService.declineInvitation(pendingInvitation.getInvitationCode(), 2L);

        assertEquals(InvitationStatus.DECLINED, pendingInvitation.getStatus());
        assertNotNull(pendingInvitation.getRespondedAt());
    }

    @Test
    void cancelInvitation_WhenInviterRequests_CancelsInvitation() {
        when(invitationRepository.findByInvitationCode(pendingInvitation.getInvitationCode()))
                .thenReturn(Optional.of(pendingInvitation));
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invitationService.cancelInvitation(pendingInvitation.getInvitationCode(), 1L);

        assertEquals(InvitationStatus.CANCELLED, pendingInvitation.getStatus());
    }

    @Test
    void cancelInvitation_WhenNonInviterRequests_ThrowsException() {
        when(invitationRepository.findByInvitationCode(pendingInvitation.getInvitationCode()))
                .thenReturn(Optional.of(pendingInvitation));

        assertThrows(IllegalStateException.class,
                () -> invitationService.cancelInvitation(pendingInvitation.getInvitationCode(), 99L));
    }

    @Test
    void acceptInvitation_WhenExpired_ThrowsException() {
        pendingInvitation.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(invitationRepository.findByInvitationCode(pendingInvitation.getInvitationCode()))
                .thenReturn(Optional.of(pendingInvitation));

        assertThrows(IllegalStateException.class,
                () -> invitationService.acceptInvitation(pendingInvitation.getInvitationCode(), 2L));
    }
}
