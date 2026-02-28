package org.example.uvi.App.Domain.Repository.UserInterestRepository;

import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUserId(Long userId);

    Optional<UserInterest> findByUserIdAndInterest(Long userId, Interest interest);

    boolean existsByUserIdAndInterest(Long userId, Interest interest);

    @Query("SELECT ui.interest FROM UserInterest ui WHERE ui.user.id = :userId")
    Set<Interest> findInterestsByUserId(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserInterest ui WHERE ui.user.id = :userId ORDER BY ui.preferenceLevel DESC, ui.lastUsedAt DESC")
    List<UserInterest> findByUserIdOrderedByPreference(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserInterest ui WHERE ui.user.id = :userId AND ui.preferenceLevel >= :minLevel")
    List<UserInterest> findByUserIdAndMinPreferenceLevel(@Param("userId") Long userId,
                                                         @Param("minLevel") Integer minLevel);

    void deleteByUserIdAndInterest(Long userId, Interest interest);

    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(ui) FROM UserInterest ui WHERE ui.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    List<UserInterest> findByInterest(Interest interest);

    @Query("SELECT ui FROM UserInterest ui WHERE ui.user.id = :userId ORDER BY ui.preferenceLevel DESC")
    List<UserInterest> findByUserIdOrderByPreferenceLevelDesc(@Param("userId") Long userId);
}
