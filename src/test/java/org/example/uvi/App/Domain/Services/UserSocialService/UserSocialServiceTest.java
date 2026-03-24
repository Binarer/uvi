package org.example.uvi.App.Domain.Services.UserSocialService;

import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Models.UserFavorite.UserFavorite;
import org.example.uvi.App.Domain.Models.UserVisit.UserVisit;
import org.example.uvi.App.Domain.Repository.UserFavoriteRepository.UserFavoriteRepository;
import org.example.uvi.App.Domain.Repository.UserVisitRepository.UserVisitRepository;
import org.example.uvi.App.Domain.Services.PlaceService.PlaceService;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSocialServiceTest {

    @Mock
    private UserFavoriteRepository favoriteRepository;
    @Mock
    private UserVisitRepository visitRepository;
    @Mock
    private UserService userService;
    @Mock
    private PlaceService placeService;

    @InjectMocks
    private UserSocialService userSocialService;

    @Test
    void addToFavorites_WhenNotExists_SavesFavorite() {
        Long userId = 1L, placeId = 10L;
        when(favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)).thenReturn(false);
        when(userService.getUserById(userId)).thenReturn(new User());
        when(placeService.getPlaceById(placeId)).thenReturn(new Place());

        userSocialService.addToFavorites(userId, placeId);

        verify(favoriteRepository).save(any(UserFavorite.class));
    }

    @Test
    void addVisit_SavesVisit() {
        Long userId = 1L, placeId = 10L;
        when(userService.getUserById(userId)).thenReturn(new User());
        when(placeService.getPlaceById(placeId)).thenReturn(new Place());
        when(visitRepository.save(any(UserVisit.class))).thenAnswer(i -> i.getArgument(0));

        UserVisit result = userSocialService.addVisit(userId, placeId, "Great!", 5);

        assertNotNull(result);
        assertEquals("Great!", result.getComment());
        assertEquals(5, result.getRating());
        verify(visitRepository).save(any(UserVisit.class));
    }

    @Test
    void getUserFavorites_ReturnsList() {
        when(favoriteRepository.findAllByUserId(1L)).thenReturn(List.of(new UserFavorite()));
        List<UserFavorite> result = userSocialService.getUserFavorites(1L);
        assertEquals(1, result.size());
    }
}
