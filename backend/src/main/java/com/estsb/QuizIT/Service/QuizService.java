package com.estsb.QuizIT.Service;

import com.estsb.QuizIT.Entity.Quiz;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    // Create a new quiz
    public Quiz createQuiz(Quiz quiz) {
        try {
            return quizRepository.save(quiz);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Retrieve all quizzes
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // Retrieve a quiz by its ID
    public Optional<Quiz> getQuizById(Long quizId) {
        return quizRepository.findById(quizId);
    }

    // Retrieve the last quiz (based on list order)
    public Optional<Quiz> getLastId() {
        List<Quiz> quizzes = quizRepository.findAll();
        return quizzes.isEmpty() ? Optional.empty() : Optional.of(quizzes.get(quizzes.size() - 1));
    }

    // Update an existing quiz by ID
    public Quiz updateQuiz(Long quizId, Quiz updatedQuiz) {
        if (quizRepository.existsById(quizId)) {
            updatedQuiz.setId(quizId);
            return quizRepository.save(updatedQuiz);
        } else {
            return null;
        }
    }

    // Delete a quiz by ID
    public boolean deleteQuiz(Long quizId) {
        if (quizRepository.existsById(quizId)) {
            quizRepository.deleteById(quizId);
            return true;
        } else {
            return false;
        }
    }

    // Set the visibility (public/private) of a quiz
    public Quiz setQuizVisibility(Long quizId, boolean isPublic) {
        return quizRepository.findById(quizId).map(quiz -> {
            quiz.setIsPublic(isPublic);
            return quizRepository.save(quiz);
        }).orElse(null);
    }

    // Search quizzes by optional filters: name, visibility, and creator
    public Map<String, List<Quiz>> searchQuizzesSeparate(
            String quizName,
            Boolean isPublic,
            User createdBy) {

        List<Quiz> allQuizzes = quizRepository.findAll();

        List<Quiz> ownQuizzes = List.of();
        List<Quiz> publicQuizzes = List.of();

        if (createdBy != null) {
            ownQuizzes = allQuizzes.stream()
                    .filter(q -> quizName == null || (q.getName() != null && q.getName().toLowerCase().contains(quizName.toLowerCase())))
                    // Include all quizzes created by the user (public or private)
                    .filter(q -> q.getCreatedBy() != null && q.getCreatedBy().equals(createdBy))
                    .collect(Collectors.toList());

            publicQuizzes = allQuizzes.stream()
                    .filter(q -> quizName == null || (q.getName() != null && q.getName().toLowerCase().contains(quizName.toLowerCase())))
                    // Only public quizzes
                    .filter(q -> Boolean.TRUE.equals(q.getIsPublic()))
                    // Exclude user's own quizzes
                    .filter(q -> q.getCreatedBy() == null || !q.getCreatedBy().equals(createdBy))
                    .collect(Collectors.toList());
        } else {
            // No user: only public quizzes matching filter
            publicQuizzes = allQuizzes.stream()
                    .filter(q -> quizName == null || (q.getName() != null && q.getName().toLowerCase().contains(quizName.toLowerCase())))
                    .filter(q -> isPublic == null || Boolean.TRUE.equals(q.getIsPublic()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Quiz>> result = new HashMap<>();
        result.put("ownQuizzes", ownQuizzes);
        result.put("publicQuizzes", publicQuizzes);

        return result;
    }

    public Map<String, List<Quiz>> findAllPublicOrOwned(User user) {
        List<Quiz> allQuizzes = quizRepository.findAll();

        List<Quiz> ownQuizzes;
        List<Quiz> publicQuizzes;

        if (user != null) {
            ownQuizzes = allQuizzes.stream()
                    .filter(q -> q.getCreatedBy() != null && q.getCreatedBy().equals(user))
                    .collect(Collectors.toList());

            publicQuizzes = allQuizzes.stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsPublic())
                            && (q.getCreatedBy() == null || !q.getCreatedBy().equals(user)))
                    .collect(Collectors.toList());
        } else {
            ownQuizzes = Collections.emptyList();
            publicQuizzes = allQuizzes.stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsPublic()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Quiz>> result = new HashMap<>();
        result.put("ownQuizzes", ownQuizzes);
        result.put("publicQuizzes", publicQuizzes);
        return result;
    }

}
