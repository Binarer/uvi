package org.example.uvi.App.Domain.Services.FamilyService;

import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;
import org.example.uvi.App.Domain.Enums.FamilyStatus.FamilyStatus;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.FamilyMemberRepository.FamilyMemberRepository;
import org.example.uvi.App.Domain.Repository.FamilyRepository.FamilyRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    @Mock private FamilyRepository familyRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private UserService userService;

    @InjectMocks private FamilyService familyService;

    private User creator;
    private Family family;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(1L).firstName("Admin").phoneNumber("79001234567")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();

        FamilyMember adminMember = FamilyMember.builder()
                .id(1L).user(creator).role(FamilyMemberRole.ADMIN).isActive(true).build();

        family = Family.builder()
                .id(1L).name("Test Family").creator(creator)
                .status(FamilyStatus.ACTIVE).maxMembers(10)
                .members(new ArrayList<>(List.of(adminMember)))
                .invitations(new ArrayList<>())
                .build();

        adminMember.setFamily(family);
    }

    @Test
    void createFamily_CreatesAndReturnsFamilyWithAdminMember() {
        when(userService.getUserById(1L)).thenReturn(creator);
        when(familyRepository.save(any())).thenAnswer(inv -> {
            Family f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });
        when(familyMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Family result = familyService.createFamily(1L, "My Family", "Description", "http://avatar.url");

        assertNotNull(result);
        assertEquals("My Family", result.getName());
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @Test
    void getFamilyById_WhenExists_ReturnsFamily() {
        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));

        Family result = familyService.getFamilyById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getFamilyById_WhenNotFound_ThrowsException() {
        when(familyRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> familyService.getFamilyById(99L));
    }

    @Test
    void updateFamily_WhenAdminRequests_UpdatesFamily() {
        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));
        when(familyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Family result = familyService.updateFamily(1L, 1L, "New Name", "New Desc", null);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void updateFamily_WhenNonAdminRequests_ThrowsException() {
        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));

        assertThrows(IllegalStateException.class,
                () -> familyService.updateFamily(1L, 99L, "New Name", null, null));
    }

    @Test
    void addMember_WhenValidRequest_AddsMember() {
        User newUser = User.builder().id(2L).firstName("New").phoneNumber("79009876543")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();

        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));
        when(userService.getUserById(2L)).thenReturn(newUser);
        when(familyMemberRepository.existsByUserAndIsActive(newUser, true)).thenReturn(false);
        when(familyMemberRepository.countActiveMembersByFamilyId(1L)).thenReturn(1L);
        when(familyMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FamilyMember result = familyService.addMember(1L, 2L, FamilyMemberRole.MEMBER, 1L);

        assertNotNull(result);
        assertEquals(FamilyMemberRole.MEMBER, result.getRole());
    }

    @Test
    void addMember_WhenAlreadyMember_ThrowsException() {
        User existing = User.builder().id(2L).build();
        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));
        when(userService.getUserById(2L)).thenReturn(existing);
        when(familyMemberRepository.existsByUserAndIsActive(existing, true)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> familyService.addMember(1L, 2L, FamilyMemberRole.MEMBER, 1L));
    }

    @Test
    void deleteFamily_WhenAdminRequests_SoftDeletesFamily() {
        when(familyRepository.findByIdActive(1L)).thenReturn(Optional.of(family));
        when(familyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        familyService.deleteFamily(1L, 1L);

        assertEquals(FamilyStatus.INACTIVE, family.getStatus());
        assertNotNull(family.getDeletedAt());
    }
}
