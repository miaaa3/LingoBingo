package com.estsb.QuizIT.Dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Long questionId;
    private String questionText;
    private List<String> options;    // multiple choice answers
    private String correctAnswer;
    private String userAnswer;

    public QuestionDTO(Long questionId, String questionText, List<String> options, String correctAnswer) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}
