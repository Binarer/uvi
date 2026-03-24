package org.example.uvi.App.Domain.Services.AuthService;

import org.example.uvi.App.Domain.Models.SmsVerification.SmsVerification;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.SmsVerificationRepository.SmsVerificationRepository;
import org.example.uvi.App.Domain.Repository.UserRepository.UserRepository;
import org.example.uvi.App.Domain.Services.RefreshTokenService.RefreshTokenService;
import org.example.uvi.App.Domain.Services.SmsService.SmsService;
import org.example.uvi.App.Domain.Services.TwoFactorService.TwoFactorService;
import org.example.uvi.App.Infrastructure.Http.Dto.AuthDto.AuthResponse;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private SmsVerificationRepository smsVerificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private SmsService smsService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TwoFactorService twoFactorService;

    @InjectMocks
    private AuthService authService;

    @Test
    void sendCode_CreatesNewVerification() {
        String phone = "79001234567";
        when(smsVerificationRepository.findLatestUnverifiedByPhoneNumber(anyString(), any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_code");

        authService.sendCode(phone);

        verify(smsVerificationRepository).save(any(SmsVerification.class));
        verify(smsService).sendVerificationCode(eq(phone), anyString());
    }

    @Test
    void verifyCode_WhenCorrect_ReturnsAuthResponse() {
        String phone = "79001234567";
        String code = "123456";
        SmsVerification verification = SmsVerification.builder()
                .phoneNumber(phone)
                .code("hashed_code")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        User user = User.builder().id(1L).phoneNumber(phone).build();

        when(smsVerificationRepository.findLatestUnverifiedByPhoneNumber(eq(phone), any())).thenReturn(Optional.of(verification));
        when(passwordEncoder.matches(code, "hashed_code")).thenReturn(true);
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any(), anyString())).thenReturn(new org.example.uvi.App.Domain.Models.RefreshToken.RefreshToken());

        AuthResponse response = authService.verifyCode(phone, code, "test-device");

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertTrue(verification.getVerified());
    }

    @Test
    void verifyCode_WhenWrongCode_ThrowsException() {
        String phone = "79001234567";
        SmsVerification verification = SmsVerification.builder()
                .code("hashed_code")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .build();

        when(smsVerificationRepository.findLatestUnverifiedByPhoneNumber(eq(phone), any())).thenReturn(Optional.of(verification));
        when(passwordEncoder.matches("wrong", "hashed_code")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.verifyCode(phone, "wrong", "dev"));
    }
}
