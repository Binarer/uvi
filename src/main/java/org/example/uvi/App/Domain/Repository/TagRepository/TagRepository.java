package org.example.uvi.App.Domain.Repository.TagRepository;

import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByInterest(Interest interest);

    List<Tag> findByInterestIn(Set<Interest> interests);

    boolean existsByName(String name);

    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findPopularTags();

    @Query("SELECT t FROM Tag t WHERE t.usageCount > :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findTagsByMinUsage(@Param("minUsage") Integer minUsage);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Tag> searchTags(@Param("query") String query);

    List<Tag> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopByOrderByUsageCountDesc(Pageable pageable);
}
