package com.estsb.QuizIT.Service;

import com.estsb.QuizIT.Entity.Question;
import com.estsb.QuizIT.Repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    // Create
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    // Create with image
    public Question createQuestionWithImage(Question question, byte[] imageBytes) {
        question.setPicture(imageBytes);
        return questionRepository.save(question);
    }

    // Read
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    // Read image bytes separately (optional)
    public byte[] getQuestionImage(Long questionId) {
        return questionRepository.findById(questionId)
                .map(Question::getPicture)
                .orElse(null);
    }

    // Update
    public Question updateQuestion(Long questionId, Question updatedQuestion) {
        if (questionRepository.existsById(questionId)) {
            updatedQuestion.setQuestionId(questionId);
            return questionRepository.save(updatedQuestion);
        } else {
            return null;
        }
    }

    // Update image bytes for a question
    public Question updateQuestionImage(Long questionId, byte[] imageBytes) {
        return questionRepository.findById(questionId).map(question -> {
            question.setPicture(imageBytes);
            return questionRepository.save(question);
        }).orElse(null);
    }

    public boolean checkUserAnswer(Question question, String userAnswer) {
        String correctAnswer= question.getCorrectAnswer();
        return correctAnswer.equalsIgnoreCase(userAnswer);
    }

    // Delete
    public boolean deleteQuestion(Long questionId) {
        if (questionRepository.existsById(questionId)) {
            questionRepository.deleteById(questionId);
            return true;
        } else {
            return false;
        }
    }
}
