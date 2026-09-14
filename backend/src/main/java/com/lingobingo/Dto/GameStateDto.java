package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Read model polled by every client (~1s) to stay in sync. Never carries correct answers. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDto {
    private String gameCode;
    private String phase;              // WAITING | ACTIVE | ENDED
    private int currentQuestionIndex;
    private int totalQuestions;
    private StateQuestion question;    // null in WAITING / ENDED
    private List<PlayerScore> players; // sorted by score desc
    private boolean currentAnswered;   // has someone answered the current question
    private String lastWinnerName;     // who took the previous question
    private int lastAnsweredIndex;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class StateQuestion {
        private int index;
        private String text;
        private List<String> options;  // no correct answer exposed
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class PlayerScore {
        private Long id;
        private String username;
        private int score;
    }
}
