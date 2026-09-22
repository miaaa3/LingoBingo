package com.lingobingo.Service;

import com.lingobingo.Entity.ActivityType;
import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Entity.Quiz;
import com.lingobingo.Entity.RecentActivity;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.FlashcardSetRepository;
import com.lingobingo.Repository.QuizRepository;
import com.lingobingo.Repository.RecentActivityRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    /** Record that a user created / updated / deleted a quiz or set. Best-effort. */
    public void log(User user, String action, ActivityType type, String title) {
        if (user == null) { return; }
        RecentActivity ra = new RecentActivity();
        ra.setUser(user);
        ra.setAction(action);
        ra.setType(type);
        ra.setTitle(title);
        ra.setTimestamp(new Date());
        recentActivityRepository.save(ra);
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

