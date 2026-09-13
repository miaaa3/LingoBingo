package com.estsb.QuizIT.Controller;

import com.estsb.QuizIT.Entity.Quiz;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.UserRepository;
import com.estsb.QuizIT.Service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;


    // Add a new quiz
    @PostMapping("/add")
    public ResponseEntity<Quiz> addQuiz(@RequestBody Quiz quiz, Principal principal) {
        if (principal != null) {
            // Get the username (email) from the principal
            String username = principal.getName();

            // Retrieve the User object from the UserRepository
            Optional<User> authenticatedUser = userRepository.findByEmail(username);

            // Check if the user is present before setting createdBy
            authenticatedUser.ifPresent(user -> quiz.setCreatedBy(user));

        }
        // Create the quiz
        Quiz createdQuiz = quizService.createQuiz(quiz);
        return new ResponseEntity<>(createdQuiz, HttpStatus.CREATED);
    }


    // Get all quizzes
    @GetMapping("/all")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        return new ResponseEntity<>(quizzes, HttpStatus.OK);
    }

    // Get a quiz by ID
    @GetMapping("getQuizById/{quizId}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long quizId) {
        Optional<Quiz> quiz = quizService.getQuizById(quizId);
        return quiz.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/last")
    public ResponseEntity<Quiz> getLastId() {
        List<Quiz> quizzes = quizService.getAllQuizzes();

        if (!quizzes.isEmpty()) {
            Quiz lastQuiz = quizzes.get(quizzes.size() - 1);
            return new ResponseEntity<>(lastQuiz, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    // Update a quiz by ID
    @PutMapping("/{quizId}/update")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long quizId, @RequestBody Quiz updatedQuiz) {
        Quiz quiz = quizService.updateQuiz(quizId, updatedQuiz);
        return (quiz != null) ?
                new ResponseEntity<>(quiz, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a quiz by ID
    @DeleteMapping("/{quizId}/delete")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long quizId) {
        boolean deleted = quizService.deleteQuiz(quizId);
        return (deleted) ?
                new ResponseEntity<>("Quiz deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
    }

    // Set quiz visibility (public/private)
    @PutMapping("/{quizId}/visibility")
    public ResponseEntity<Quiz> setQuizVisibility(@PathVariable Long quizId, @RequestParam boolean isPublic) {
        Quiz quiz = quizService.setQuizVisibility(quizId, isPublic);
        return (quiz != null) ?
                new ResponseEntity<>(quiz, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Search quizzes by optional filters: quizName, isPublic, createdBy
    @GetMapping("/search-separate")
    public ResponseEntity<Map<String, List<Quiz>>> searchQuizzesSeparate(
            @RequestParam(required = false) String quizName,
            @RequestParam(required = false) Boolean isPublic,
            Principal principal) {

        User user = null;
        if (principal != null) {
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isPresent()) user = userOpt.get();
        }

        Map<String, List<Quiz>> results = quizService.searchQuizzesSeparate(quizName, isPublic, user);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/visible")
    public ResponseEntity<Map<String, List<Quiz>>>  getVisibleQuizzes(Principal principal) {
        User user = null;
        if (principal != null) {
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isPresent()) user = userOpt.get();
        }
        Map<String, List<Quiz>> result = quizService.findAllPublicOrOwned(user);
        return ResponseEntity.ok(result);
    }

}

