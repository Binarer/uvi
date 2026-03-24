package org.example.uvi.App.Domain.Models.User;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_phone", columnList = "phoneNumber", unique = true),
        @Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 100)
    private String username;

    @Column
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 255)
    private String city;

    @Column(nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserInterest> userInterests = new HashSet<>();

    public void addInterest(UserInterest userInterest) {
        this.userInterests.add(userInterest);
        userInterest.setUser(this);
    }

    public void removeInterest(UserInterest userInterest) {
        this.userInterests.remove(userInterest);
        userInterest.setUser(null);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
