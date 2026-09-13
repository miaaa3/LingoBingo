package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClassroomDto {
    private Long id;
    private String name;
    private String description;
    private String joinCode;
    private String teacherName;
    private long memberCount;
    private LocalDateTime createdAt;
}
