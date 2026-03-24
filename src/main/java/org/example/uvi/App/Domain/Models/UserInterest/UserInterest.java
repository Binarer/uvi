package org.example.uvi.App.Domain.Models.UserInterest;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.User.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_interests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest"}),
        indexes = {
                @Index(name = "idx_user_interest_user", columnList = "user_id"),
                @Index(name = "idx_user_interest_interest", columnList = "interest")
        })
@Getter
@Setter
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserInterest that = (UserInterest) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
