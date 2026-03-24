package org.example.uvi.App.Domain.Repository.PlaceRepository;

import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByTypeAndIsActiveTrue(PlaceType type);

    List<Place> findByIsActiveTrueOrderByCreatedAtDesc();

    @Query(value = "SELECT p.* FROM places p WHERE p.is_active = true " +
            "AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters) " +
            "ORDER BY ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
            nativeQuery = true)
    List<Place> findNearbyPlaces(@Param("latitude") double latitude,
                                 @Param("longitude") double longitude,
                                 @Param("radiusInMeters") double radiusInMeters);

    @Query(value = "SELECT p.* FROM places p WHERE p.is_active = true AND p.type = :type " +
            "AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters) " +
            "ORDER BY ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
            nativeQuery = true)
    List<Place> findNearbyPlacesByType(@Param("latitude") double latitude,
                                       @Param("longitude") double longitude,
                                       @Param("radiusInMeters") double radiusInMeters,
                                       @Param("type") String type);

    @Query(value = "SELECT p.*, ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) as distance " +
            "FROM places p WHERE p.is_active = true ORDER BY distance LIMIT :limit",
            nativeQuery = true)
    List<Place> findNearestPlaces(@Param("latitude") double latitude,
                                  @Param("longitude") double longitude,
                                  @Param("limit") int limit);

    List<Place> findByCreatedById(Long userId);

    List<Place> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    @Query(value = "SELECT DISTINCT p.* FROM places p " +
            "JOIN place_tags pt ON p.id = pt.place_id " +
            "WHERE pt.tag_id IN :tagIds AND p.is_active = true",
            nativeQuery = true)
    List<Place> findByTagsIn(@Param("tagIds") Set<Long> tagIds);

    @Query(value = "SELECT DISTINCT p.* FROM places p " +
            "JOIN place_tags pt ON p.id = pt.place_id " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE t.interest IN :interests AND p.is_active = true " +
            "ORDER BY p.created_at DESC",
            nativeQuery = true)
    List<Place> findByInterests(@Param("interests") List<String> interests);

    @Query(value = "SELECT DISTINCT p.* FROM places p " +
            "JOIN place_tags pt ON p.id = pt.place_id " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE t.interest IN :interests AND p.is_active = true " +
            "AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters) " +
            "ORDER BY ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
            nativeQuery = true)
    List<Place> findByInterestsNearby(@Param("interests") List<String> interests,
                                      @Param("latitude") double latitude,
                                      @Param("longitude") double longitude,
                                      @Param("radiusInMeters") double radiusInMeters);

    @Query(value = "SELECT p.*, COUNT(pt.tag_id) as matching_tags " +
            "FROM places p " +
            "JOIN place_tags pt ON p.id = pt.place_id " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE t.interest IN :interests AND p.is_active = true " +
            "AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters) " +
            "GROUP BY p.id " +
            "ORDER BY matching_tags DESC, ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Place> findRecommendedPlaces(@Param("interests") List<String> interests,
                                      @Param("latitude") double latitude,
                                      @Param("longitude") double longitude,
                                      @Param("radiusInMeters") double radiusInMeters,
                                      @Param("limit") int limit);

    @Query(value = "SELECT DISTINCT p.* FROM places p " +
            "JOIN place_tags pt ON p.id = pt.place_id " +
            "WHERE pt.tag_id = :tagId AND p.is_active = true",
            nativeQuery = true)
    List<Place> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT p FROM Place p WHERE p.isActive = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Place> searchPlaces(@Param("query") String query);
}
