package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClassContentDto {
    private Long id;
    private String contentType;
    private Long contentId;
    private String title;
    private Long classId;
    private String className;
    private LocalDateTime assignedAt;
}
