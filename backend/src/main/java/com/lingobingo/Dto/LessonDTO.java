package com.lingobingo.Dto;

import com.lingobingo.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {
    private String title;
    private String description;
    private Boolean isPublic;
    private Boolean isFavorite;
    @JsonIgnore
    private List<SectionDTO> sections;
}
