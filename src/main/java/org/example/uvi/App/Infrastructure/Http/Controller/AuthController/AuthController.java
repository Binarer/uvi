package org.example.uvi.App.Infrastructure.Http.Controller.AuthController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.AuthService.AuthService;
import org.example.uvi.App.Infrastructure.Http.Dto.AuthResponse;
import org.example.uvi.App.Infrastructure.Http.Dto.SendSmsRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.VerifySmsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "SMS-based authentication with JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    @Operation(summary = "Send SMS verification code",
            description = "Sends a 6-digit code to the provided phone number. Rate limited to 3 requests per 5 minutes.")
    public ResponseEntity<Map<String, String>> sendCode(
            @Valid @RequestBody SendSmsRequest request) {
        authService.sendCode(request.phoneNumber());
        return ResponseEntity.ok(Map.of(
                "message", "Verification code sent successfully",
                "phone", request.phoneNumber()
        ));
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Verify SMS code and get JWT token",
            description = "Verifies the 6-digit SMS code and returns a JWT access token. " +
                    "Creates a new user if not exists.")
    public ResponseEntity<AuthResponse> verifyCode(
            @Valid @RequestBody VerifySmsRequest request) {
        String token = authService.verifyCode(request.phoneNumber(), request.code());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
