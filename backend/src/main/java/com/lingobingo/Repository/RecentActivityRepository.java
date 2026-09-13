package com.lingobingo.Repository;

import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Entity.Quiz;
import com.lingobingo.Entity.RecentActivity;
import com.lingobingo.Entity.User;
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
