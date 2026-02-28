package org.example.uvi.App.Domain.Services.UserInterestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.example.uvi.App.Domain.Repository.UserInterestRepository.UserInterestRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserService userService;

    public List<UserInterest> getUserInterests(Long userId) {
        return userInterestRepository.findByUserIdOrderedByPreference(userId);
    }

    @Transactional
    public UserInterest addInterest(Long userId, Interest interest, int preferenceLevel) {
        if (userInterestRepository.existsByUserIdAndInterest(userId, interest)) {
            throw new IllegalArgumentException("User already has this interest: " + interest);
        }

        User user = userService.getUserById(userId);

        UserInterest ui = UserInterest.builder()
                .user(user)
                .interest(interest)
                .preferenceLevel(Math.max(1, Math.min(10, preferenceLevel)))
                .build();

        return userInterestRepository.save(ui);
    }

    @Transactional
    public void setInterests(Long userId, Set<Interest> interests) {
        userInterestRepository.deleteByUserId(userId);

        User user = userService.getUserById(userId);
        List<UserInterest> newInterests = interests.stream()
                .map(interest -> UserInterest.builder()
                        .user(user)
                        .interest(interest)
                        .preferenceLevel(5)
                        .build())
                .toList();

        userInterestRepository.saveAll(newInterests);
        log.info("Set {} interests for user {}", interests.size(), userId);
    }

    @Transactional
    public void removeInterest(Long userId, Interest interest) {
        if (!userInterestRepository.existsByUserIdAndInterest(userId, interest)) {
            throw new IllegalArgumentException("User does not have this interest: " + interest);
        }
        userInterestRepository.deleteByUserIdAndInterest(userId, interest);
    }

    @Transactional
    public UserInterest increasePreference(Long userId, Interest interest) {
        UserInterest ui = userInterestRepository.findByUserIdAndInterest(userId, interest)
                .orElseThrow(() -> new IllegalArgumentException("Interest not found for user"));
        ui.increasePreference();
        ui.updateLastUsed();
        return userInterestRepository.save(ui);
    }

    @Transactional
    public UserInterest decreasePreference(Long userId, Interest interest) {
        UserInterest ui = userInterestRepository.findByUserIdAndInterest(userId, interest)
                .orElseThrow(() -> new IllegalArgumentException("Interest not found for user"));
        ui.decreasePreference();
        return userInterestRepository.save(ui);
    }

    public Set<Interest> getUserInterestEnums(Long userId) {
        return userInterestRepository.findInterestsByUserId(userId);
    }
}
