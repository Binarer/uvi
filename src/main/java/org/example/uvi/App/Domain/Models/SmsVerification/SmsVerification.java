package org.example.uvi.App.Domain.Models.SmsVerification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_verifications")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 72)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private Integer attempts;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canAttempt() {
        return attempts == null || attempts < 3;
    }

    public void incrementAttempts() {
        attempts = (attempts == null) ? 1 : attempts + 1;
    }
}
