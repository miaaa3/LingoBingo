package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PlanningEventDto {
    private Long id;
    private String title;
    private String description;
    private String type;
    private LocalDateTime dueDate;
    private Long classId;
    private String className;
}
