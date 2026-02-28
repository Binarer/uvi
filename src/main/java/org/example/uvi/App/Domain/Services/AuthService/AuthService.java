package org.example.uvi.App.Domain.Services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.SmsVerification.SmsVerification;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.SmsVerificationRepository.SmsVerificationRepository;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.example.uvi.App.Domain.Services.SmsService.SmsService;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис аутентификации через SMS + JWT.
 * Шаг 1: sendCode() — генерирует 6-значный код и отправляет SMS
 * Шаг 2: verifyCode() — проверяет код, создаёт/активирует пользователя, возвращает JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SmsVerificationRepository smsVerificationRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private static final int CODE_EXPIRY_MINUTES = 5;
    private final SecureRandom random = new SecureRandom();

    /**
     * Отправляет SMS-код верификации на указанный номер.
     * Создаёт запись SmsVerification в БД.
     */
    @Transactional
    public void sendCode(String phoneNumber) {
        String normalized = normalizePhone(phoneNumber);

        // Инвалидируем предыдущие неверифицированные коды для этого номера
        smsVerificationRepository.findLatestUnverifiedByPhoneNumber(normalized, LocalDateTime.now())
                .ifPresent(v -> {
                    v.setExpiresAt(LocalDateTime.now().minusSeconds(1));
                    smsVerificationRepository.save(v);
                });

        String code = generateCode();
        SmsVerification verification = SmsVerification.builder()
                .phoneNumber(normalized)
                .code(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .verified(false)
                .attempts(0)
                .build();

        smsVerificationRepository.save(verification);
        smsService.sendVerificationCode(normalized, code);

        log.info("Sent verification code to {}", normalized);
    }

    /**
     * Верифицирует SMS-код и возвращает JWT-токен.
     * Если пользователь не существует — создаёт нового.
     *
     * @return JWT access token
     */
    @Transactional
    public String verifyCode(String phoneNumber, String code) {
        String normalized = normalizePhone(phoneNumber);

        SmsVerification verification = smsVerificationRepository
                .findLatestUnverifiedByPhoneNumber(normalized, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active verification code for this number"));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("Verification code has expired");
        }

        if (!verification.canAttempt()) {
            throw new IllegalArgumentException("Too many verification attempts");
        }

        verification.incrementAttempts();

        if (!passwordEncoder.matches(code, verification.getCode())) {
            smsVerificationRepository.save(verification);
            throw new IllegalArgumentException("Invalid verification code");
        }

        // Помечаем верификацию успешной
        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        smsVerificationRepository.save(verification);

        // Находим или создаём пользователя
        User user = userRepository.findByPhoneNumberActive(normalized)
                .orElseGet(() -> createNewUser(normalized));

        user.setPhoneVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getPhoneNumber());
        log.info("User {} verified and authenticated", user.getId());

        return token;
    }

    private User createNewUser(String phoneNumber) {
        log.info("Creating new user for phone {}", phoneNumber);
        return userRepository.save(User.builder()
                .phoneNumber(phoneNumber)
                .firstName("User")
                .passwordHash(passwordEncoder.encode(generateCode()))
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .phoneVerified(false)
                .build());
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private String normalizePhone(String phone) {
        if (phone == null) throw new IllegalArgumentException("Phone number cannot be null");
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            digits = "7" + digits.substring(1);
        }
        if (digits.length() != 11 || !digits.startsWith("7")) {
            throw new IllegalArgumentException("Invalid phone number format. Expected: 79XXXXXXXXX");
        }
        return digits;
    }
}
