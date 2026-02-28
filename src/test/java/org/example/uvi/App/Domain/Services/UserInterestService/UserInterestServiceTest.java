package org.example.uvi.App.Domain.Services.UserInterestService;

import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.example.uvi.App.Domain.Repository.UserInterestRepository.UserInterestRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInterestServiceTest {

    @Mock private UserInterestRepository userInterestRepository;
    @Mock private UserService userService;

    @InjectMocks private UserInterestService userInterestService;

    private User testUser;
    private UserInterest jazzInterest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).firstName("Ivan").phoneNumber("79001234567")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();

        jazzInterest = UserInterest.builder()
                .id(1L).user(testUser).interest(Interest.JAZZ).preferenceLevel(7).build();
    }

    @Test
    void addInterest_WhenNew_SavesInterest() {
        when(userInterestRepository.existsByUserIdAndInterest(1L, Interest.JAZZ)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(userInterestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserInterest result = userInterestService.addInterest(1L, Interest.JAZZ, 7);

        assertNotNull(result);
        assertEquals(Interest.JAZZ, result.getInterest());
        assertEquals(7, result.getPreferenceLevel());
    }

    @Test
    void addInterest_WhenAlreadyExists_ThrowsException() {
        when(userInterestRepository.existsByUserIdAndInterest(1L, Interest.JAZZ)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userInterestService.addInterest(1L, Interest.JAZZ, 5));
    }

    @Test
    void removeInterest_WhenExists_DeletesInterest() {
        when(userInterestRepository.existsByUserIdAndInterest(1L, Interest.JAZZ)).thenReturn(true);

        userInterestService.removeInterest(1L, Interest.JAZZ);

        verify(userInterestRepository).deleteByUserIdAndInterest(1L, Interest.JAZZ);
    }

    @Test
    void removeInterest_WhenNotExists_ThrowsException() {
        when(userInterestRepository.existsByUserIdAndInterest(1L, Interest.JAZZ)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userInterestService.removeInterest(1L, Interest.JAZZ));
    }

    @Test
    void increasePreference_WhenExists_IncreasesLevel() {
        when(userInterestRepository.findByUserIdAndInterest(1L, Interest.JAZZ))
                .thenReturn(Optional.of(jazzInterest));
        when(userInterestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserInterest result = userInterestService.increasePreference(1L, Interest.JAZZ);

        assertEquals(8, result.getPreferenceLevel());
    }

    @Test
    void decreasePreference_WhenExists_DecreasesLevel() {
        when(userInterestRepository.findByUserIdAndInterest(1L, Interest.JAZZ))
                .thenReturn(Optional.of(jazzInterest));
        when(userInterestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserInterest result = userInterestService.decreasePreference(1L, Interest.JAZZ);

        assertEquals(6, result.getPreferenceLevel());
    }

    @Test
    void setInterests_ReplacesAllInterests() {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(userInterestRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        userInterestService.setInterests(1L, Set.of(Interest.JAZZ, Interest.ROCK_MUSIC));

        verify(userInterestRepository).deleteByUserId(1L);
        verify(userInterestRepository).saveAll(any());
    }

    @Test
    void getUserInterests_ReturnsOrderedList() {
        when(userInterestRepository.findByUserIdOrderedByPreference(1L))
                .thenReturn(List.of(jazzInterest));

        List<UserInterest> result = userInterestService.getUserInterests(1L);

        assertEquals(1, result.size());
        assertEquals(Interest.JAZZ, result.get(0).getInterest());
        verify(userInterestRepository).findByUserIdOrderedByPreference(1L);
    }
}