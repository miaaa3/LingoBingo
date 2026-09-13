package com.estsb.QuizIT.Service;

import com.estsb.QuizIT.Entity.FlashcardSet;
import com.estsb.QuizIT.Entity.Quiz;
import com.estsb.QuizIT.Entity.RecentActivity;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.FlashcardSetRepository;
import com.estsb.QuizIT.Repository.QuizRepository;
import com.estsb.QuizIT.Repository.RecentActivityRepository;
import com.estsb.QuizIT.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecentActivityService {

    private final RecentActivityRepository recentActivityRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    public List<RecentActivity> getRecentActivitiesForUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return recentActivityRepository.findByUserOrderByTimestampDesc(user);
    }

    public RecentActivity saveRecentActivity(RecentActivity recentActivity) {
        recentActivityRepository.save(recentActivity);
        return recentActivity;
    }

    public List<RecentActivity> getRecentActivitiesForQuiz(Long quizId) {
        Optional<Quiz> quiz = quizRepository.findById(quizId);
        return recentActivityRepository.findByQuizOrderByTimestampDesc(quiz);
    }

    public List<RecentActivity> getRecentActivitiesForFlashcardSet(Long flashcardSetId) {
        Optional<FlashcardSet> flashcardSet = flashcardSetRepository.findById(flashcardSetId);
        return recentActivityRepository.findByFlashcardSetOrderByTimestampDesc(flashcardSet);
    }

}

