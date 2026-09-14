package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One completed language-practice session. Records the XP earned so the
 * dashboard can fold practice into the same points/level/streak loop as
 * quizzes, without touching QuizResult.
 */
@Data
@Entity
@Table(name = "practice_sessions")
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private int xp;
    private int correct;
    private int total;

    private LocalDateTime submittedAt = LocalDateTime.now();
}
