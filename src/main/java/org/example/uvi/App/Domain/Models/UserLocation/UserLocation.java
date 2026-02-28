package org.example.uvi.App.Domain.Models.UserLocation;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Models.User.User;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "coordinates", columnDefinition = "geography(POINT,4326)", nullable = false)
    private Point coordinates;

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "speed")
    private Float speed;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
