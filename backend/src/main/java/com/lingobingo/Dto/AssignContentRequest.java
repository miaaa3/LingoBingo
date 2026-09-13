package com.lingobingo.Dto;

import lombok.Data;

@Data
public class AssignContentRequest {
    private Long classId;
    private String contentType; // COURSE, QUIZ, FLASHCARD_SET
    private Long contentId;
}
