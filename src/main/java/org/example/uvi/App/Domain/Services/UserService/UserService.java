package org.example.uvi.App.Domain.Services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhoneNumberActive(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found with phone: " + phone));
    }

    public List<User> getAllUsers() {
        return userRepository.findAllActive();
    }

    public List<User> searchByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, User updated) {
        User user = getUserById(id);

        if (updated.getFirstName() != null) user.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) user.setLastName(updated.getLastName());
        if (updated.getDateOfBirth() != null) user.setDateOfBirth(updated.getDateOfBirth());
        if (updated.getCity() != null) user.setCity(updated.getCity());
        if (updated.getLatitude() != null) user.setLatitude(updated.getLatitude());
        if (updated.getLongitude() != null) user.setLongitude(updated.getLongitude());
        if (updated.getUsername() != null) {
            if (userRepository.existsByUsername(updated.getUsername())) {
                throw new IllegalArgumentException("Username already taken: " + updated.getUsername());
            }
            user.setUsername(updated.getUsername());
        }

        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User {} soft-deleted", id);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User activateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
