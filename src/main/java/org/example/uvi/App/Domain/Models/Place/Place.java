package org.example.uvi.App.Domain.Models.Place;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Models.User.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "places", indexes = {
        @Index(name = "idx_place_type", columnList = "type"),
        @Index(name = "idx_place_created_by", columnList = "created_by_id"),
        @Index(name = "idx_place_location", columnList = "location")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PlaceType type;

    @Column(length = 500)
    private String address;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "main_photo_url", length = 1000)
    private String mainPhotoUrl;

    @ElementCollection
    @CollectionTable(name = "place_photos", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "photo_url", length = 1000)
    @Builder.Default
    private List<String> photos = new ArrayList<>();

    @Column(name = "color", length = 7)
    @Builder.Default
    private String color = "#22C55E";

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;    

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "place_tags",
            joinColumns = @JoinColumn(name = "place_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_place_tags_place", columnList = "place_id"),
                    @Index(name = "idx_place_tags_tag", columnList = "tag_id")
            }
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getPlaces().add(this);
        tag.incrementUsageCount();
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getPlaces().remove(this);
        tag.decrementUsageCount();
    }

    @PrePersist
    @PreUpdate
    private void updateCoordinates() {
        if (location != null) {
            this.latitude = location.getY();
            this.longitude = location.getX();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Place place = (Place) o;
        return getId() != null && Objects.equals(getId(), place.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
