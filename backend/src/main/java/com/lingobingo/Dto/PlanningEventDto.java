package com.lingobingo.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    // Force an ISO string on the wire (e.g. "2026-09-18T15:00:00") so the
    // Angular date pipe can parse it. Without this the value can serialize as a
    // numeric array, which breaks rendering (NG02100).
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;
    private Long classId;
    private String className;
}
