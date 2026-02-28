package org.example.uvi.App.Infrastructure.Http.Controller.AuthController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.AuthService.AuthService;
import org.example.uvi.App.Domain.Services.TwoFactorService.TwoFactorService;
import org.example.uvi.App.Infrastructure.Http.Dto.*;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication: SMS OTP + Google 2FA + JWT refresh")
public class AuthController {

    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    private final JwtService jwtService;

    @PostMapping("/send-code")
    @Operation(summary = "Send SMS verification code")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendSmsRequest request) {
        authService.sendCode(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Verify SMS code and get tokens")
    public ResponseEntity<AuthResponse> verifyCode(
            @Valid @RequestBody VerifySmsRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.verifyCode(request.phoneNumber(), request.code(), deviceInfo));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.refreshTokens(request.refreshToken(), deviceInfo));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revoke all refresh tokens", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "Setup Google 2FA — returns QR code URL", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TwoFactorSetupResponse> setup2fa(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String qrUrl = twoFactorService.setupTwoFactor(userId);
        // Извлекаем secret из URL для ручного ввода
        String secret = qrUrl.contains("secret=")
                ? qrUrl.split("secret=")[1].split("&")[0]
                : "";
        return ResponseEntity.ok(new TwoFactorSetupResponse(qrUrl, secret));
    }

    @PostMapping("/2fa/confirm")
    @Operation(summary = "Confirm 2FA setup with first TOTP code", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> confirm2fa(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean ok = twoFactorService.confirmTwoFactor(userId, request.code());
        if (!ok) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify TOTP code after SMS auth (when 2FA is enabled)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AuthResponse> verify2fa(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        String deviceInfo = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.verifyTwoFactor(userId, request.code(), deviceInfo));
    }

    @DeleteMapping("/2fa")
    @Operation(summary = "Disable 2FA", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> disable2fa(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        twoFactorService.disableTwoFactor(userId);
        return ResponseEntity.ok().build();
    }
}
