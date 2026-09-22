package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Table(name = "flashcard_sets")
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private Boolean isFavorite = false;

    @JsonManagedReference(value = "flashcard-cards")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "flashcardSet" )
    private List<Flashcard> flashcards;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-flashcard")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "flashcardSet")
    @JsonBackReference(value = "flashcard-set-games")
    private List<Game> games;

    @Column(nullable = false)
    private Boolean isPublic = true;

    // Language pair for translation decks (ISO codes, e.g. "en" / "fr").
    // Null for non-language sets; drives auto-translate and practice direction.
    private String sourceLang;
    private String targetLang;

    // Read-only owner identity for the client. `createdBy` itself is hidden by
    // @JsonBackReference, so these expose just enough for the UI to decide who
    // may edit/delete without leaking the full user graph. No setters: Jackson
    // treats them as read-only and ignores them on the way back in.
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
