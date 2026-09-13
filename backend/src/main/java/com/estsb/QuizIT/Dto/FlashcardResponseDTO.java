package com.estsb.QuizIT.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardResponseDTO {
    private String term;
    private String definition;
    private String mediaFilename; // Only metadata
    private String mediaType;     // MIME type

}
