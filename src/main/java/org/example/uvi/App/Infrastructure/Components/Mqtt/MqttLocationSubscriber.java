package org.example.uvi.App.Infrastructure.Components.Mqtt;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Models.UserLocation.UserLocation;
import org.example.uvi.App.Domain.Services.UserLocationService.UserLocationService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserLocationMapper.UserLocationMapper;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Подписчик на MQTT-топик location/#.
 * При получении сообщения:
 *  1. Сохраняет геолокацию пользователя в БД
 *  2. Бродкастит обновление подписчикам WebSocket /topic/location/{userId}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttLocationSubscriber {

    private final UserLocationService userLocationService;
    private final UserLocationMapper userLocationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleLocationMessage(Message<String> message) {
        try {
            MqttLocationMessage locationMessage =
                    objectMapper.readValue(message.getPayload(), MqttLocationMessage.class);

            UserLocation saved = userLocationService.saveLocation(
                    locationMessage.getUserId(),
                    locationMessage.getLatitude(),
                    locationMessage.getLongitude(),
                    locationMessage.getAccuracy(),
                    locationMessage.getBatteryLevel(),
                    locationMessage.getSpeed()
            );

            // Бродкаст подписчикам WebSocket
            UserLocationDto dto = userLocationMapper.toDto(saved);
            messagingTemplate.convertAndSend(
                    "/topic/location/" + locationMessage.getUserId(), dto);

            log.debug("Saved and broadcasted location for user {} via MQTT",
                    locationMessage.getUserId());
        } catch (Exception e) {
            log.error("Failed to process MQTT location message: {}", e.getMessage(), e);
        }
    }
}
