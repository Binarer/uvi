package org.example.uvi.App.Domain.Repository.FamilyInvitationRepository;

import org.example.uvi.App.Domain.Enums.InvitationStatus.InvitationStatus;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.Family.FamilyInvitation;
import org.example.uvi.App.Domain.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyInvitationRepository extends JpaRepository<FamilyInvitation, Long> {

    Optional<FamilyInvitation> findByInvitationCode(String invitationCode);

    List<FamilyInvitation> findByFamily(Family family);

    List<FamilyInvitation> findByFamilyAndStatus(Family family, InvitationStatus status);

    List<FamilyInvitation> findByInviteeAndStatus(User invitee, InvitationStatus status);

    @Query("SELECT fi FROM FamilyInvitation fi WHERE fi.inviteePhone = :phone " +
            "AND fi.status = 'PENDING' AND fi.expiresAt > :now")
    List<FamilyInvitation> findActiveInvitationsByPhone(@Param("phone") String phone,
                                                        @Param("now") LocalDateTime now);

    @Query("SELECT fi FROM FamilyInvitation fi WHERE fi.status = 'PENDING' AND fi.expiresAt < :now")
    List<FamilyInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(fi) > 0 FROM FamilyInvitation fi WHERE fi.family.id = :familyId " +
            "AND fi.invitee.id = :inviteeId AND fi.status = 'PENDING' AND fi.expiresAt > :now")
    boolean existsActiveInvitation(@Param("familyId") Long familyId,
                                   @Param("inviteeId") Long inviteeId,
                                   @Param("now") LocalDateTime now);
}
