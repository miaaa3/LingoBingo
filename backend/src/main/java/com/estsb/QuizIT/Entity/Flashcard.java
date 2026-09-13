package com.estsb.QuizIT.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "Flashcards")
@AllArgsConstructor
@NoArgsConstructor
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private String definition;

    @Lob
    @Column(name = "media", columnDefinition = "LONGBLOB")
    private byte[] media; // Binary content stored directly in DB

    private String mediaFilename;
    private String mediaType;



    @ManyToOne
    @JoinColumn(name = "flashcard_set_id")
    @JsonBackReference(value = "flashcard-cards")
    private FlashcardSet flashcardSet;
}
