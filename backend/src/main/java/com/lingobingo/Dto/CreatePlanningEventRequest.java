package com.lingobingo.Dto;

import lombok.Data;

@Data
public class CreatePlanningEventRequest {
    private Long classId;
    private String title;
    private String description;
    private String type;
    private String dueDate; // ISO local date-time, e.g. 2026-09-20T14:30
}
