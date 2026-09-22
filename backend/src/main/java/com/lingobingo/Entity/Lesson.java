package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "lessons")
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "lesson-sections")
    private List<Section> sections;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-lessons")
    private User createdBy;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(nullable = false)
    private Boolean isFavorite = true;

    // Read-only owner identity for the client (createdBy is hidden by
    // @JsonBackReference). No setters, so these are ignored on deserialization.
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getOwnerEmail() {
        return createdBy != null ? createdBy.getEmail() : null;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getOwnerId() {
        return createdBy != null ? createdBy.getId() : null;
    }
}
