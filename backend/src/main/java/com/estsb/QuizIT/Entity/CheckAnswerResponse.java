package com.estsb.QuizIT.Entity;

import lombok.Data;

@Data
public class CheckAnswerResponse {
    private final boolean correct;
    private final String correctAnswer;
    private final String message;

    public CheckAnswerResponse(boolean correct, String correctAnswer, String message) {
        this.correct = correct;
        this.correctAnswer= correctAnswer;
        this.message = message;
    }
}