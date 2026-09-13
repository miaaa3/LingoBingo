package com.estsb.QuizIT.Dto;

import com.estsb.QuizIT.Entity.ActivityType;
import lombok.Data;

@Data
public class RecentActivityDTO {
    private Long userId;
    private ActivityType activityType;
    private Long entityId; // This can be either quizId or flashcardSetId
    private Long flashcardSetId; // Only used when activityType is FLASHCARD_SET

}
