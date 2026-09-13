package com.estsb.QuizIT.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardSetResponseDTO {
    private Long id;                 // Flashcard set ID
    private String title;            // Set title
    private String description;
    @JsonIgnore
    private List<FlashcardResponseDTO> flashcards;
}