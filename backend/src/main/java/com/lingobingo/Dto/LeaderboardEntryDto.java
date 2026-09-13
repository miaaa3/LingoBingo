package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private String name;
    private int points;
    private boolean isMe;
}
