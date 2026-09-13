package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quiz_results")
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private int score;
    private int maxScore;
    private int correctCount;
    private int totalQuestions;

    private LocalDateTime submittedAt = LocalDateTime.now();
}
