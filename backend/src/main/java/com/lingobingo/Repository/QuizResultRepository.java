package com.lingobingo.Repository;

import com.lingobingo.Entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuizId(Long quizId);
    List<QuizResult> findByStudentEmail(String email);
}
