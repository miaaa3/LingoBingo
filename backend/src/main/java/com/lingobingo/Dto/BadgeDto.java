package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeDto {
    private String key;
    private String label;
    private boolean earned;
}
