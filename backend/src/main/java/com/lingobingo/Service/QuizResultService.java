package com.lingobingo.Service;

import com.lingobingo.Dto.QuizResultDto;
import com.lingobingo.Dto.SubmitQuizResponse;
import com.lingobingo.Entity.Question;
import com.lingobingo.Entity.Quiz;
import com.lingobingo.Entity.QuizResult;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.QuizRepository;
import com.lingobingo.Repository.QuizResultRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizResultService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubmitQuizResponse submit(Long quizId, String studentEmail, Map<String, String> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        // Deadline passed: the quiz is closed, no more submissions.
        if (quiz.getDueDate() != null && LocalDateTime.now().isAfter(quiz.getDueDate())) {
            throw new IllegalStateException("This quiz is closed");
        }

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        int score = 0, maxScore = 0, correct = 0, total = 0;
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                int pts = (q.getPoints() != null) ? q.getPoints() : 1;
                maxScore += pts;
                total++;
                String submitted = (answers != null) ? answers.get(String.valueOf(q.getQuestionId())) : null;
                if (submitted != null && submitted.equalsIgnoreCase(q.getCorrectAnswer())) {
                    score += pts;
                    correct++;
                }
            }
        }

        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setStudent(student);
        result.setScore(score);
        result.setMaxScore(maxScore);
        result.setCorrectCount(correct);
        result.setTotalQuestions(total);
        quizResultRepository.save(result);

        return new SubmitQuizResponse(score, maxScore, correct, total);
    }

    public List<QuizResultDto> resultsForQuiz(Long quizId) {
        return quizResultRepository.findByQuizId(quizId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<QuizResultDto> resultsForStudent(String email) {
        return quizResultRepository.findByStudentEmail(email).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private QuizResultDto toDto(QuizResult r) {
        String name = (r.getStudent() != null) ? r.getStudent().getName() : null;
        String email = (r.getStudent() != null) ? r.getStudent().getEmail() : null;
        String quizName = (r.getQuiz() != null) ? r.getQuiz().getName() : null;
        return new QuizResultDto(name, email, r.getScore(), r.getMaxScore(),
                r.getCorrectCount(), r.getTotalQuestions(), r.getSubmittedAt(), quizName);
    }
}
