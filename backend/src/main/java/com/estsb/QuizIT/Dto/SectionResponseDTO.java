package com.estsb.QuizIT.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionResponseDTO {
    private String heading;
    private String content;
    private String mediaFilename;
    private String mediaType;
}