package org.example.uvi.App.Domain.Models.UserInterest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.User.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_interests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest"}),
        indexes = {
                @Index(name = "idx_user_interest_user", columnList = "user_id"),
                @Index(name = "idx_user_interest_interest", columnList = "interest")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Interest interest;

    @Column(name = "preference_level")
    @Builder.Default
    private Integer preferenceLevel = 5;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void increasePreference() {
        if (this.preferenceLevel < 10) {
            this.preferenceLevel++;
        }
    }

    public void decreasePreference() {
        if (this.preferenceLevel > 1) {
            this.preferenceLevel--;
        }
    }
}
