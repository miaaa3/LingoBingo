package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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


}
