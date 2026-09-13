package com.estsb.QuizIT.Repository;

import com.estsb.QuizIT.Entity.FlashcardSet;
import com.estsb.QuizIT.Entity.Quiz;
import com.estsb.QuizIT.Entity.RecentActivity;
import com.estsb.QuizIT.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentActivityRepository extends JpaRepository<RecentActivity, Long> {
    List<RecentActivity> findByQuizOrderByTimestampDesc(Optional<Quiz> quiz);

    List<RecentActivity> findByFlashcardSetOrderByTimestampDesc(Optional<FlashcardSet> flashcardSet);

    List<RecentActivity> findByUserOrderByTimestampDesc(Optional<User> user);
}
