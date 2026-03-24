package org.example.uvi.App.Domain.Repository.UserFavoriteRepository;

import org.example.uvi.App.Domain.Models.UserFavorite.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findAllByUserId(Long userId);
    Optional<UserFavorite> findByUserIdAndPlaceId(Long userId, Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
}
