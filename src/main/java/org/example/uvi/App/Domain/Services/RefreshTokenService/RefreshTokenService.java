package org.example.uvi.App.Domain.Services.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Models.RefreshToken.RefreshToken;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.RefreshTokenRepository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshExpirationMs;

    // Максимум активных refresh токенов на пользователя (защита от утечки)
    private static final int MAX_ACTIVE_TOKENS = 5;

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo) {
        // Если токенов слишком много — отзываем все старые
        long activeCount = refreshTokenRepository.countActiveTokensByUserId(user.getId(), Instant.now());
        if (activeCount >= MAX_ACTIVE_TOKENS) {
            log.warn("User {} has {} active refresh tokens, revoking all", user.getId(), activeCount);
            refreshTokenRepository.revokeAllByUserId(user.getId());
        }

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .createdAt(Instant.now())
                .deviceInfo(deviceInfo)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken validateAndRotate(String tokenValue, User user, String deviceInfo) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!token.isValid()) {
            // Если токен скомпрометирован — отзываем все токены пользователя
            refreshTokenRepository.revokeAllByUserId(token.getUser().getId());
            throw new IllegalArgumentException("Refresh token is expired or revoked. All sessions terminated.");
        }

        // Rotation: отзываем старый, создаём новый
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return createRefreshToken(user, deviceInfo);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All refresh tokens revoked for user {}", userId);
    }

    @Transactional
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
    }

    // Чистим устаревшие токены каждые 24 часа
    @Scheduled(fixedRate = 86_400_000)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up expired/revoked refresh tokens");
    }
}
