package com.lingobingo.Entity.Requests;

import lombok.Data;

@Data
public class UserAnswerRequest {
    private Long questionId;
    private String userAnswer;
}
