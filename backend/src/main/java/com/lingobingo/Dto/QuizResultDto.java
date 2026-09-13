package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QuizResultDto {
    private String studentName;
    private String studentEmail;
    private int score;
    private int maxScore;
    private int correctCount;
    private int totalQuestions;
    private LocalDateTime submittedAt;
    private String quizName;
}
