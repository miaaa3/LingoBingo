package com.estsb.QuizIT.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponseDTO {
    private Long id;
    private String title;
    private Boolean isPublic;
    private Boolean isFavorite;
    @JsonIgnore
    private List<SectionResponseDTO> sections;
}