package com.lingobingo.Dto;

import com.lingobingo.Entity.Flashcard;
import lombok.Data;

@Data
public class AnswerRequest {
    private Flashcard flashcard;
    private Boolean isCorrect;
    private Integer timeSpent;
}
