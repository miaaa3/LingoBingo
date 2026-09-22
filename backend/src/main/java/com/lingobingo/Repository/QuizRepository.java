package com.lingobingo.Repository;

import com.lingobingo.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz,Long> {
    // Quizzes whose deadline has passed but whose teacher hasn't been told yet.
    List<Quiz> findByDueDateBeforeAndClosedNotifiedFalse(LocalDateTime cutoff);

    // Quizzes falling due within a window whose students haven't been reminded yet.
    List<Quiz> findByDueDateBetweenAndDueSoonNotifiedFalse(LocalDateTime start, LocalDateTime end);
}
