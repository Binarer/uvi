package org.example.uvi.App.Domain.Services.UserSocialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Models.UserFavorite.UserFavorite;
import org.example.uvi.App.Domain.Models.UserVisit.UserVisit;
import org.example.uvi.App.Domain.Repository.UserFavoriteRepository.UserFavoriteRepository;
import org.example.uvi.App.Domain.Repository.UserVisitRepository.UserVisitRepository;
import org.example.uvi.App.Domain.Services.PlaceService.PlaceService;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSocialService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserVisitRepository visitRepository;
    private final UserService userService;
    private final PlaceService placeService;

    @Transactional
    public void addToFavorites(Long userId, Long placeId) {
        if (favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            return;
        }
        User user = userService.getUserById(userId);
        Place place = placeService.getPlaceById(placeId);

        UserFavorite favorite = UserFavorite.builder()
                .user(user)
                .place(place)
                .build();
        favoriteRepository.save(favorite);
        log.info("Place {} added to favorites for user {}", placeId, userId);
    }

    @Transactional
    public void removeFromFavorites(Long userId, Long placeId) {
        favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
        log.info("Place {} removed from favorites for user {}", placeId, userId);
    }

    public List<UserFavorite> getUserFavorites(Long userId) {
        return favoriteRepository.findAllByUserId(userId);
    }

    @Transactional
    public UserVisit addVisit(Long userId, Long placeId, String comment, Integer rating) {
        User user = userService.getUserById(userId);
        Place place = placeService.getPlaceById(placeId);

        UserVisit visit = UserVisit.builder()
                .user(user)
                .place(place)
                .comment(comment)
                .rating(rating)
                .build();
        log.info("Visit recorded for user {} at place {}", userId, placeId);
        return visitRepository.save(visit);
    }

    public List<UserVisit> getUserVisits(Long userId) {
        return visitRepository.findAllByUserId(userId);
    }
}
