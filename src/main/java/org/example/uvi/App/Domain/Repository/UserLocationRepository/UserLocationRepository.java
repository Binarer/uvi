package org.example.uvi.App.Domain.Repository.UserLocationRepository;

import org.example.uvi.App.Domain.Models.UserLocation.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {

    Optional<UserLocation> findTopByUserIdOrderByTimestampDesc(Long userId);

    List<UserLocation> findAllByUserIdOrderByTimestampDesc(Long userId);

    @Query(value = """
            SELECT ul.* FROM user_locations ul
            WHERE ul.user_id = :userId
              AND ul.timestamp BETWEEN :from AND :to
            ORDER BY ul.timestamp DESC
            """, nativeQuery = true)
    List<UserLocation> findByUserIdAndTimeRange(@Param("userId") Long userId,
                                                @Param("from") Instant from,
                                                @Param("to") Instant to);

    @Query(value = """
            SELECT ul.* FROM user_locations ul
            WHERE ST_DWithin(
                ul.coordinates::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY ul.timestamp DESC
            """, nativeQuery = true)
    List<UserLocation> findUsersNearLocation(@Param("lat") double lat,
                                             @Param("lon") double lon,
                                             @Param("radiusMeters") double radiusMeters);
}
