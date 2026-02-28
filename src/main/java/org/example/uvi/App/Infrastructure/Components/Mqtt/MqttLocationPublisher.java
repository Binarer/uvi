package org.example.uvi.App.Infrastructure.Components.Mqtt;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttLocationPublisher {

    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;

    /**
     * Публикует сообщение о местоположении пользователя в MQTT-топик location/{userId}.
     */
    public void publishLocation(MqttLocationMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            String topic = "location/" + message.getUserId();

            mqttOutboundChannel.send(
                    MessageBuilder.withPayload(payload)
                            .setHeader(MqttHeaders.TOPIC, topic)
                            .setHeader(MqttHeaders.QOS, 1)
                            .build()
            );

            log.debug("Published location for user {} to topic {}", message.getUserId(), topic);
        } catch (JacksonException e) {
            log.error("Failed to serialize location message: {}", e.getMessage(), e);
        }
    }
}
