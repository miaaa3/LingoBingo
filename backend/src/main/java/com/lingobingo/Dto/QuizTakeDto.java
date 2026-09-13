package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QuizTakeDto {
    private Long id;
    private String name;
    private String description;
    private List<TakeQuestion> questions;

    @Data
    @AllArgsConstructor
    public static class TakeQuestion {
        private Long questionId;
        private String questionText;
        private String questionDescription;
        private Map<String, String> answers;
        private Integer points;
    }
}
