package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardDto {
    private String name;
    private int points;
    private int level;
    private int xpIntoLevel;
    private int xpForLevel;
    private int streak;
    private int quizzesDone;
    private int rank;          // 0 = not in a class
    private String className;  // null = not in a class
    private List<BadgeDto> badges;
    private List<LeaderboardEntryDto> leaderboard;
    private List<PlanningEventDto> upcoming;
}
