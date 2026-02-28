package org.example.uvi.App.Domain.Repository.FamilyRepository;

import org.example.uvi.App.Domain.Enums.FamilyStatus.FamilyStatus;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    List<Family> findByCreator(User creator);

    List<Family> findByStatus(FamilyStatus status);

    @Query("SELECT f FROM Family f WHERE f.deletedAt IS NULL AND f.status = 'ACTIVE'")
    List<Family> findAllActive();

    @Query("SELECT f FROM Family f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Family> findByIdActive(@Param("id") Long id);

    @Query("SELECT DISTINCT f FROM Family f " +
            "JOIN f.members m " +
            "WHERE m.user.id = :userId AND m.isActive = true AND f.deletedAt IS NULL")
    List<Family> findFamiliesByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Family f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND f.deletedAt IS NULL")
    List<Family> searchByName(@Param("searchTerm") String searchTerm);
}
