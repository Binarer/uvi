package org.example.uvi.App.Domain.Models.Tag;

import jakarta.persistence.*;
import lombok.*;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_interest", columnList = "interest"),
        @Index(name = "idx_tag_name", columnList = "name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Interest interest;

    @Column(length = 500)
    private String description;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Place> places = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public void decrementUsageCount() {
        this.usageCount = (this.usageCount == null || this.usageCount <= 0) ? 0 : this.usageCount - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
