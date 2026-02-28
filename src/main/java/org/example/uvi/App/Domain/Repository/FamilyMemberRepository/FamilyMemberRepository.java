package org.example.uvi.App.Domain.Repository.FamilyMemberRepository;

import org.example.uvi.App.Domain.Enums.FamilyMemberRole.FamilyMemberRole;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyMember;
import org.example.uvi.App.Domain.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByFamily(Family family);

    List<FamilyMember> findByFamilyAndIsActive(Family family, Boolean isActive);

    List<FamilyMember> findByFamilyAndRole(Family family, FamilyMemberRole role);

    List<FamilyMember> findByUser(User user);

    @Query("SELECT fm FROM FamilyMember fm WHERE fm.family.id = :familyId " +
            "AND fm.user.id = :userId AND fm.isActive = true")
    Optional<FamilyMember> findByFamilyIdAndUserId(@Param("familyId") Long familyId,
                                                    @Param("userId") Long userId);

    boolean existsByFamilyAndUserAndIsActive(Family family, User user, Boolean isActive);

    @Query("SELECT COUNT(fm) FROM FamilyMember fm WHERE fm.family.id = :familyId AND fm.isActive = true")
    Long countActiveMembersByFamilyId(@Param("familyId") Long familyId);
}
