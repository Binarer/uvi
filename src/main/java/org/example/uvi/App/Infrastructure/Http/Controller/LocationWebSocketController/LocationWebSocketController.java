package org.example.uvi.App.Infrastructure.Http.Controller.LocationWebSocketController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.UserLocationService.UserLocationService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationDto;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationRequest;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserLocationMapper.UserLocationMapper;
import org.example.uvi.App.Infrastructure.Components.Mqtt.MqttLocationMessage;
import org.example.uvi.App.Infrastructure.Components.Mqtt.MqttLocationPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP WebSocket контроллер для real-time трекинга локаций.
 *
 * Клиент подключается через: ws://host/ws (SockJS fallback)
 *
 * Публикация локации (клиент → сервер):
 *   SEND /app/location.update
 *   Body: { "latitude": 55.7, "longitude": 37.6, "accuracy": 5.0, "batteryLevel": 80, "speed": 0.0 }
 *
 * Подписка на локацию конкретного пользователя (клиент → брокер):
 *   SUBSCRIBE /topic/location/{userId}
 *
 * Подписка на всех пользователей семьи (клиент → брокер):
 *   SUBSCRIBE /topic/family/{familyId}/locations
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "WebSocket Location", description = "Real-time location via STOMP/WebSocket")
public class LocationWebSocketController {

    private final UserLocationService userLocationService;
    private final UserLocationMapper userLocationMapper;
    private final MqttLocationPublisher mqttLocationPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Клиент отправляет свою локацию через WebSocket.
     * Сервер сохраняет в БД, публикует в MQTT и бродкастит подписчикам.
     */
    @MessageMapping("/location.update")
    @Operation(summary = "Send location update via WebSocket")
    public void updateLocation(UserLocationRequest request, Principal principal) {
        Long userId = (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        // Сохраняем в БД
        var saved = userLocationService.saveLocation(
                userId,
                request.latitude(),
                request.longitude(),
                request.accuracy(),
                request.batteryLevel(),
                request.speed()
        );

        UserLocationDto dto = userLocationMapper.toDto(saved);

        // Рассылаем подписчикам топика этого пользователя
        messagingTemplate.convertAndSend("/topic/location/" + userId, dto);

        // Также публикуем в MQTT для других систем
        mqttLocationPublisher.publishLocation(MqttLocationMessage.builder()
                .userId(userId)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .accuracy(request.accuracy())
                .batteryLevel(request.batteryLevel())
                .speed(request.speed())
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * Подписка на обновления локации конкретного пользователя.
     * Клиент: SUBSCRIBE /topic/location/{userId}
     * Сервер возвращает последнюю известную локацию сразу при подписке.
     */
    @MessageMapping("/location.subscribe/{userId}")
    @SendTo("/topic/location/{userId}")
    @Operation(summary = "Subscribe to user location updates")
    public UserLocationDto subscribeToUserLocation(@DestinationVariable Long userId) {
        return userLocationService.getLatestLocation(userId)
                .map(userLocationMapper::toDto)
                .orElse(null);
    }
}
