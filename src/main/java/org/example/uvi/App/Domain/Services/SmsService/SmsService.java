package org.example.uvi.App.Domain.Services.SmsService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Сервис отправки SMS через SMS Aero API.
 * В режиме разработки (sms.development_mode=true) только логирует код, не отправляет SMS.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${sms.development_mode:true}")
    private boolean developmentMode;

    @Value("${sms.aero.api_key:}")
    private String apiKey;

    @Value("${sms.aero.email:}")
    private String email;

    private static final String SMS_AERO_BASE_URL = "https://gate.smsaero.ru/v2";

    /**
     * Отправляет SMS с кодом верификации на указанный номер телефона.
     * @param phoneNumber номер телефона в формате 79XXXXXXXXX
     * @param code        6-значный код верификации
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        if (developmentMode) {
            log.info("[DEV MODE] SMS to {}: Your verification code is {}", phoneNumber, code);
            return;
        }

        try {
            String message = "Your UVI verification code: " + code;

            RestClient restClient = RestClient.builder()
                    .baseUrl(SMS_AERO_BASE_URL)
                    .build();

            restClient.post()
                    .uri("/sms/send")
                    .header("Authorization", buildBasicAuth())
                    .body(Map.of(
                            "number", phoneNumber,
                            "text", message,
                            "sign", "SMS Aero"
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.info("SMS sent successfully to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification SMS: " + e.getMessage());
        }
    }

    private String buildBasicAuth() {
        String credentials = email + ":" + apiKey;
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
