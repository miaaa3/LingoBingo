package com.lingobingo.Controller;


import com.lingobingo.Dto.RecentActivityDTO;
import com.lingobingo.Entity.*;
import com.lingobingo.Repository.FlashcardSetRepository;
import com.lingobingo.Repository.QuizRepository;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.RecentActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recent-activities")
public class RecentActivityController {
    private final RecentActivityService recentActivityService;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveRecentActivity(@RequestBody RecentActivityDTO recentActivityDTO, Principal principal) {
        if (principal == null) {
            // User not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            // User not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Create a RecentActivity instance from the DTO
        RecentActivity recentActivity = new RecentActivity();
        recentActivity.setUser(user);
        recentActivity.setType(recentActivityDTO.getActivityType());
        recentActivity.setTimestamp(new Date()); // Set the current timestamp

        // Set either quiz or flashcardSet based on the activity type
        if (recentActivityDTO.getActivityType() == ActivityType.QUIZ) {
            Long quizId = recentActivityDTO.getEntityId();
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            recentActivity.setTitle(quiz.getName());

            if (quiz == null) {
                // Quiz not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            recentActivity.setQuiz(quiz);
        } else if (recentActivityDTO.getActivityType() == ActivityType.FLASHCARD_SET) {
            Long flashcardSetId = recentActivityDTO.getEntityId();
            FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId).orElse(null);
            recentActivity.setTitle(flashcardSet.getTitle());

            if (flashcardSet == null) {
                // FlashcardSet not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            recentActivity.setFlashcardSet(flashcardSet);
        } else {
            // Unsupported activity type
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Save the recent activity
        recentActivityService.saveRecentActivity(recentActivity);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("")
    public List<RecentActivity> getRecentActivitiesForUser(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Long userId = userRepository.findByEmail(username).get().getId();
            return recentActivityService.getRecentActivitiesForUser(userId);
        } else {
            return Collections.emptyList();
        }
    }
    @GetMapping("/quiz/{quizId}")
    public List<RecentActivity> getRecentActivitiesForQuiz(@PathVariable Long quizId) {
        return recentActivityService.getRecentActivitiesForQuiz(quizId);
    }

    @GetMapping("/flashcard-set/{flashcardSetId}")
    public List<RecentActivity> getRecentActivitiesForFlashcardSet(@PathVariable Long flashcardSetId) {
        return recentActivityService.getRecentActivitiesForFlashcardSet(flashcardSetId);
    }

}
