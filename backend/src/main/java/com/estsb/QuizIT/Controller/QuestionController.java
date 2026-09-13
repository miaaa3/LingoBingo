package com.estsb.QuizIT.Controller;

import com.estsb.QuizIT.Entity.CheckAnswerResponse;
import com.estsb.QuizIT.Entity.Question;
import com.estsb.QuizIT.Entity.Requests.UserAnswerRequest;
import com.estsb.QuizIT.Service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/questions")
public class QuestionController {

    private final QuestionService questionService;

    // Create a new question (without image)
    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        Question createdQuestion = questionService.createQuestion(question);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    // Create a new question with image upload
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Question> createQuestionWithImage(
            @RequestPart("question") Question question,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        question.setPicture(imageFile.getBytes());
        Question createdQuestion = questionService.createQuestion(question);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    // Get question image by questionId
    @GetMapping("/{questionId}/image")
    public ResponseEntity<byte[]> getQuestionImage(@PathVariable Long questionId) {
        Optional<Question> questionOpt = questionService.getQuestionById(questionId);
        if (questionOpt.isEmpty() || questionOpt.get().getPicture() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] imageData = questionOpt.get().getPicture();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or PNG depending on your images
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    // Check the correctness of the user's answer
    @PostMapping("/check-answer")
    public ResponseEntity<com.estsb.QuizIT.Entity.CheckAnswerResponse> checkAnswer(@RequestBody UserAnswerRequest userAnswerRequest) {
        try {
            Optional<Question> optionalQuestion = questionService.getQuestionById(userAnswerRequest.getQuestionId());
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                boolean isCorrect = questionService.checkUserAnswer(question, userAnswerRequest.getUserAnswer());
                String correctAnswer = question.getCorrectAnswer();

                return ResponseEntity.ok(new CheckAnswerResponse(isCorrect, correctAnswer, "Answer checked successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CheckAnswerResponse(false, null, "Question not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CheckAnswerResponse(false, null, "Error checking answer: " + e.getMessage()));
        }
    }

    // Get all questions
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    // Get a question by ID
    @GetMapping("/{questionId}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long questionId) {
        Optional<Question> question = questionService.getQuestionById(questionId);
        return question.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update a question by ID (without image update)
    @PutMapping("/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @RequestBody Question updatedQuestion) {
        Question question = questionService.updateQuestion(questionId, updatedQuestion);
        return (question != null) ?
                new ResponseEntity<>(question, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update question image only
    @PutMapping(value = "/{questionId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Question> updateQuestionImage(
            @PathVariable Long questionId,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        Question updatedQuestion = questionService.updateQuestionImage(questionId, imageFile.getBytes());
        if (updatedQuestion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedQuestion);
    }

    // Delete a question by ID
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long questionId) {
        boolean deleted = questionService.deleteQuestion(questionId);
        return (deleted) ?
                new ResponseEntity<>("Question deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("Question not found", HttpStatus.NOT_FOUND);
    }
}
