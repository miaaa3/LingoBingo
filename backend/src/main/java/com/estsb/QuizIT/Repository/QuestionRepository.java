package com.estsb.QuizIT.Repository;

import com.estsb.QuizIT.Entity.Question;
import com.estsb.QuizIT.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    List<Question> findByQuiz(Quiz quiz);

    @Query(value = "SELECT DISTINCT qa.answer FROM question_answers qa WHERE qa.question_id <> :questionId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<String> findRandomAnswersExcluding(@Param("questionId") Long questionId, @Param("limit") int limit);
}
