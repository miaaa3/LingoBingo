package com.estsb.QuizIT.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "games")
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonBackReference(value = "user-games")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "game_players",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> players;

    // Optional: FlashcardSet used in the game (nullable)
    @ManyToOne
    @JoinColumn(name = "flashcard_set_id", nullable = true)
    @JsonBackReference(value = "flashcard-set-games")
    private FlashcardSet flashcardSet;

    // Optional: Quiz used in the game (nullable)
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = true)
    @JsonBackReference(value = "quiz-games")
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType; // TERM, DEFINITION, RANDOM (adapt as needed)

    private Boolean isActive = true;

    private String gameCode;

    @ElementCollection
    @MapKeyColumn(name = "player_id")
    @Column(name = "score")
    private Map<Long, Integer> playerScores;

    @ElementCollection
    @MapKeyColumn(name = "player_id")
    @Column(name = "finish_time")
    private Map<Long, Long> playerFinishTimes;

    @ElementCollection
    @MapKeyColumn(name = "player_id")
    @Column(name = "question_ids")
    private Map<Long, List<Long>> playerQuestionIds;

    @ElementCollection
    @MapKeyColumn(name = "player_id")
    @Column(name = "current_question_index")
    private Map<Long, Integer> playerCurrentQuestionIndex;


    @Column(nullable = false)
    private int numberOfQuestions;

    // Add a helper method to check if the game is flashcard or quiz based
    public boolean isFlashcardGame() {
        return flashcardSet != null;
    }

    public boolean isQuizGame() {
        return quiz != null;
    }

}
