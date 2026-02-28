package org.example.uvi.App.Infrastructure.Components.Mqtt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqttLocationMessage {
    private Long userId;
    private double latitude;
    private double longitude;
    private Float accuracy;
    private Integer batteryLevel;
    private Float speed;
    private long timestamp;
}
