package org.example.uvi.App.Domain.Services.TwoFactorService;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final UserRepository userRepository;

    @Value("${app.2fa.issuer:UVI}")
    private String issuer;

    /**
     * Генерирует новый секретный ключ 2FA для пользователя.
     * Возвращает URL для QR-кода (для Google Authenticator).
     */
    @Transactional
    public String setupTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GoogleAuthenticatorKey key = gAuth.createCredentials();
        user.setTwoFactorSecret(key.getKey());
        user.setTwoFactorEnabled(false); // включится после подтверждения
        userRepository.save(user);

        String accountName = user.getPhoneNumber();
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, accountName, key);
    }

    /**
     * Подтверждает настройку 2FA — проверяет первый код и включает 2FA.
     */
    @Transactional
    public boolean confirmTwoFactor(Long userId, int code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("2FA not initialized for this user");
        }

        boolean valid = gAuth.authorize(user.getTwoFactorSecret(), code);
        if (valid) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            log.info("2FA enabled for user {}", userId);
        }
        return valid;
    }

    /**
     * Верифицирует TOTP код при входе.
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    /**
     * Отключает 2FA для пользователя.
     */
    @Transactional
    public void disableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        log.info("2FA disabled for user {}", userId);
    }
}
