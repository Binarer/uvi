package org.example.uvi.App.Domain.Models.Device;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Enums.OsType.OsType;
import org.example.uvi.App.Domain.Models.User.User;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false, length = 512)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "os_type", nullable = false)
    private OsType osType;

    @UpdateTimestamp
    @Column(name = "last_active_at")
    private Instant lastActiveAt;
}
