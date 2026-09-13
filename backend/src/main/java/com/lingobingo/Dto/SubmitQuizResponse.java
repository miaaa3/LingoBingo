package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmitQuizResponse {
    private int score;
    private int maxScore;
    private int correctCount;
    private int totalQuestions;
}
