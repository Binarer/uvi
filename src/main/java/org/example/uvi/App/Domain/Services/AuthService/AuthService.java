package org.example.uvi.App.Domain.Services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.RefreshToken.RefreshToken;
import org.example.uvi.App.Domain.Models.SmsVerification.SmsVerification;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.SmsVerificationRepository.SmsVerificationRepository;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.example.uvi.App.Domain.Services.RefreshTokenService.RefreshTokenService;
import org.example.uvi.App.Domain.Services.SmsService.SmsService;
import org.example.uvi.App.Domain.Services.TwoFactorService.TwoFactorService;
import org.example.uvi.App.Infrastructure.Http.Dto.AuthResponse;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SmsVerificationRepository smsVerificationRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TwoFactorService twoFactorService;

    /**
     * Шаг 1: отправить SMS с кодом верификации.
     */
    @Transactional
    public void sendCode(String phoneNumber) {
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        // Обновляем существующую запись или создаём новую
        smsVerificationRepository
                .findLatestUnverifiedByPhoneNumber(phoneNumber, LocalDateTime.now())
                .ifPresentOrElse(
                        existing -> {
                            existing.setCode(passwordEncoder.encode(code));
                            existing.setExpiresAt(expiry);
                            existing.setVerified(false);
                            existing.setAttempts(0);
                            smsVerificationRepository.save(existing);
                        },
                        () -> smsVerificationRepository.save(
                                SmsVerification.builder()
                                        .phoneNumber(phoneNumber)
                                        .code(passwordEncoder.encode(code))
                                        .expiresAt(expiry)
                                        .verified(false)
                                        .build()
                        )
                );

        smsService.sendVerificationCode(phoneNumber, code);
        log.info("Verification code sent to {}", phoneNumber);
    }

    /**
     * Шаг 2: верифицировать SMS код.
     * Если у пользователя включён 2FA — возвращает tempToken для второго шага.
     * Иначе — возвращает access + refresh токены.
     */
    @Transactional
    public AuthResponse verifyCode(String phoneNumber, String code, String deviceInfo) {
        SmsVerification verification = smsVerificationRepository
                .findLatestUnverifiedByPhoneNumber(phoneNumber, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Код не найден или истёк. Запросите новый."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("Код истёк. Запросите новый.");
        }

        if (!verification.canAttempt()) {
            throw new IllegalArgumentException("Превышено количество попыток. Запросите новый код.");
        }

        verification.incrementAttempts();

        if (!passwordEncoder.matches(code, verification.getCode())) {
            smsVerificationRepository.save(verification);
            throw new IllegalArgumentException("Неверный код верификации.");
        }

        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        smsVerificationRepository.save(verification);

        // Найти или создать пользователя
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .phoneNumber(phoneNumber)
                            .status(UserStatus.ACTIVE)
                            .phoneVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        user.setPhoneVerified(true);
        userRepository.save(user);

        // Если 2FA включён — возвращаем tempToken
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String tempToken = jwtService.generateAccessToken(user.getId(), user.getPhoneNumber());
            log.info("2FA required for user {}", user.getId());
            return AuthResponse.twoFactorRequired(tempToken);
        }

        return issueTokens(user, deviceInfo);
    }

    /**
     * Шаг 2б (если включён 2FA): верифицировать TOTP код.
     */
    @Transactional
    public AuthResponse verifyTwoFactor(Long userId, int totpCode, String deviceInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled()) || user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("2FA не настроен для этого пользователя");
        }

        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), totpCode)) {
            throw new IllegalArgumentException("Неверный код Google Authenticator");
        }

        return issueTokens(user, deviceInfo);
    }

    /**
     * Обновить access token по refresh token (rotation).
     */
    @Transactional
    public AuthResponse refreshTokens(String refreshTokenValue, String deviceInfo) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        User user = refreshToken.getUser();

        RefreshToken newRefreshToken = refreshTokenService.validateAndRotate(refreshTokenValue, user, deviceInfo);
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getPhoneNumber());

        log.info("Tokens refreshed for user {}", user.getId());
        return AuthResponse.of(newAccessToken, newRefreshToken.getToken(), jwtService.getAccessExpirationMs());
    }

    /**
     * Выход — отзываем все refresh токены пользователя.
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("User {} logged out", userId);
    }

    private AuthResponse issueTokens(User user, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhoneNumber());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, deviceInfo);
        return AuthResponse.of(accessToken, refreshToken.getToken(), jwtService.getAccessExpirationMs());
    }
}
