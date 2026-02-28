package org.example.uvi.App.Domain.Services.UserService;

import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Petrov")
                .phoneNumber("79001234567")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .phoneVerified(true)
                .build();
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ivan", result.getFirstName());
    }

    @Test
    void getUserById_WhenUserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(99L));
    }

    @Test
    void getUserById_WhenUserSoftDeleted_ThrowsException() {
        testUser.setDeletedAt(LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsers_ReturnsActiveUsers() {
        when(userRepository.findAllActive()).thenReturn(List.of(testUser));

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateUser_WhenValidData_UpdatesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updates = User.builder()
                .firstName("Petr")
                .lastName("Ivanov")
                .city("Moscow")
                .build();

        User result = userService.updateUser(1L, updates);

        assertEquals("Petr", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("Moscow", result.getCity());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUsernameAlreadyTaken_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        User updates = User.builder().username("taken").build();

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updates));
    }

    @Test
    void deleteUser_SoftDeletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.deleteUser(1L);

        assertNotNull(testUser.getDeletedAt());
        assertEquals(UserStatus.INACTIVE, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void activateUser_ChangesStatusToActive() {
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.activateUser(1L);

        assertEquals(UserStatus.ACTIVE, result.getStatus());
    }

    @Test
    void deactivateUser_ChangesStatusToInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.deactivateUser(1L);

        assertEquals(UserStatus.INACTIVE, result.getStatus());
    }

    @Test
    void searchByName_ReturnsMachingUsers() {
        when(userRepository.findByNameContaining("Ivan")).thenReturn(List.of(testUser));

        List<User> result = userService.searchByName("Ivan");

        assertEquals(1, result.size());
        assertEquals("Ivan", result.get(0).getFirstName());
    }
}
