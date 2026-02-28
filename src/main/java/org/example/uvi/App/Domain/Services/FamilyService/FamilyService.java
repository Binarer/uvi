package org.example.uvi.App.Domain.Services.FamilyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;
import org.example.uvi.App.Domain.Enums.FamilyStatus.FamilyStatus;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.FamilyMemberRepository.FamilyMemberRepository;
import org.example.uvi.App.Domain.Repository.FamilyRepository.FamilyRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserService userService;

    @Transactional
    public Family createFamily(Long creatorId, String name, String description) {
        User creator = userService.getUserById(creatorId);

        Family family = Family.builder()
                .name(name)
                .description(description)
                .creator(creator)
                .status(FamilyStatus.ACTIVE)
                .build();

        family = familyRepository.save(family);

        FamilyMember creatorMember = FamilyMember.builder()
                .family(family)
                .user(creator)
                .role(FamilyMemberRole.ADMIN)
                .isActive(true)
                .build();

        familyMemberRepository.save(creatorMember);
        log.info("Family '{}' created by user {}", name, creatorId);

        return family;
    }

    public Family getFamilyById(Long id) {
        return familyRepository.findByIdActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Family not found: " + id));
    }

    public List<Family> getAllFamilies() {
        return familyRepository.findAllActive();
    }

    public List<Family> getFamiliesByUser(Long userId) {
        return familyRepository.findFamiliesByUserId(userId);
    }

    public List<Family> getFamiliesByCreator(Long creatorId) {
        User creator = userService.getUserById(creatorId);
        return familyRepository.findByCreator(creator);
    }

    @Transactional
    public Family updateFamily(Long id, Long requesterId, String name, String description, String avatarUrl) {
        Family family = getFamilyById(id);
        checkAdminAccess(family, requesterId);

        if (name != null && !name.isBlank()) family.setName(name);
        if (description != null) family.setDescription(description);
        if (avatarUrl != null) family.setAvatarUrl(avatarUrl);

        return familyRepository.save(family);
    }

    @Transactional
    public void deleteFamily(Long id, Long requesterId) {
        Family family = getFamilyById(id);
        checkAdminAccess(family, requesterId);

        family.setStatus(FamilyStatus.INACTIVE);
        family.setDeletedAt(LocalDateTime.now());
        familyRepository.save(family);
        log.info("Family {} soft-deleted by user {}", id, requesterId);
    }

    @Transactional
    public FamilyMember addMember(Long familyId, Long userId, FamilyMemberRole role, Long requesterId) {
        Family family = getFamilyById(familyId);
        checkAdminAccess(family, requesterId);

        User user = userService.getUserById(userId);

        if (familyMemberRepository.existsByFamilyAndUserAndIsActive(family, user, true)) {
            throw new IllegalStateException("User is already a member of this family");
        }

        long currentCount = familyMemberRepository.countActiveMembersByFamilyId(familyId);
        if (currentCount >= family.getMaxMembers()) {
            throw new IllegalStateException("Family has reached maximum member limit: " + family.getMaxMembers());
        }

        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role(role)
                .isActive(true)
                .build();

        return familyMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long familyId, Long memberId, Long requesterId) {
        Family family = getFamilyById(familyId);
        checkAdminAccess(family, requesterId);

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Family member not found: " + memberId));

        if (!member.getFamily().getId().equals(familyId)) {
            throw new IllegalArgumentException("Member does not belong to this family");
        }

        if (member.getUser().getId().equals(family.getCreator().getId())) {
            throw new IllegalStateException("Cannot remove the family creator");
        }

        member.setIsActive(false);
        member.setLeftAt(LocalDateTime.now());
        familyMemberRepository.save(member);
    }

    @Transactional
    public FamilyMember updateMemberRole(Long familyId, Long memberId, FamilyMemberRole role, Long requesterId) {
        Family family = getFamilyById(familyId);
        checkAdminAccess(family, requesterId);

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Family member not found: " + memberId));

        member.setRole(role);
        return familyMemberRepository.save(member);
    }

    public List<FamilyMember> getFamilyMembers(Long familyId) {
        Family family = getFamilyById(familyId);
        return familyMemberRepository.findByFamilyAndIsActive(family, true);
    }

    private void checkAdminAccess(Family family, Long userId) {
        boolean isAdmin = family.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId)
                        && m.getIsActive()
                        && m.getRole() == FamilyMemberRole.ADMIN);
        boolean isCreator = family.getCreator().getId().equals(userId);

        if (!isAdmin && !isCreator) {
            throw new IllegalStateException("User does not have admin access to this family");
        }
    }
}
