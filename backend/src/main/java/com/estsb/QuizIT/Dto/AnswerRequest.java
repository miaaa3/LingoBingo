package com.estsb.QuizIT.Dto;

import com.estsb.QuizIT.Entity.Flashcard;
import lombok.Data;

@Data
public class AnswerRequest {
    private Flashcard flashcard;
    private Boolean isCorrect;
    private Integer timeSpent;
}
