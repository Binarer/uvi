package org.example.uvi.App.Domain.Models.UserLocation;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Models.User.User;
import org.hibernate.proxy.HibernateProxy;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_locations", indexes = {
    @Index(name = "idx_user_location_user_time", columnList = "user_id, timestamp DESC"),
    @Index(name = "idx_user_location_coords", columnList = "coordinates")
})
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserLocation that = (UserLocation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
