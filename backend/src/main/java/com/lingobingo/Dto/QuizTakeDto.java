package com.lingobingo.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QuizTakeDto {
    private Long id;
    private String name;
    private String description;

    // Deadline, if any. The take page uses it to show and enforce "closed".
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime dueDate;

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
