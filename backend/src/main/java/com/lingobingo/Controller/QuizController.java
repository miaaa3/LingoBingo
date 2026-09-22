package com.lingobingo.Controller;

import com.lingobingo.Entity.ActivityType;
import com.lingobingo.Entity.Quiz;
import com.lingobingo.Entity.Question;
import com.lingobingo.Dto.QuizTakeDto;
import com.lingobingo.Entity.Role;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.QuizService;
import com.lingobingo.Service.QuizResultService;
import com.lingobingo.Service.RecentActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final QuizResultService quizResultService;
    private final RecentActivityService recentActivityService;

    private User currentUser(Principal principal) {
        return (principal != null) ? userRepository.findByEmail(principal.getName()).orElse(null) : null;
    }

    /** Only the quiz's creator (or an admin) may manage it. */
    private boolean canManage(Quiz quiz, User user) {
        if (quiz == null || user == null) return false;
        if (user.getRole() == Role.ADMIN) return true;
        return quiz.getCreatedBy() != null && user.getId() != null
                && user.getId().equals(quiz.getCreatedBy().getId());
    }


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
        recentActivityService.log(currentUser(principal), "CREATED", ActivityType.QUIZ, createdQuiz.getName());
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


    // Update a quiz by ID - owner (or admin) only
    @PutMapping("/{quizId}/update")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long quizId, @RequestBody Quiz updatedQuiz, Principal principal) {
        Quiz existing = quizService.getQuizById(quizId).orElse(null);
        if (existing == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        Quiz quiz = quizService.updateQuiz(quizId, updatedQuiz);
        if (quiz != null) {
            recentActivityService.log(currentUser(principal), "UPDATED", ActivityType.QUIZ, quiz.getName());
        }
        return (quiz != null) ?
                new ResponseEntity<>(quiz, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a quiz by ID - owner (or admin) only
    @DeleteMapping("/{quizId}/delete")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long quizId, Principal principal) {
        Quiz existing = quizService.getQuizById(quizId).orElse(null);
        if (existing == null) return new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>("Not your quiz", HttpStatus.FORBIDDEN);

        String name = existing.getName();
        boolean deleted = quizService.deleteQuiz(quizId);
        if (deleted) {
            recentActivityService.log(currentUser(principal), "DELETED", ActivityType.QUIZ, name);
        }
        return (deleted) ?
                new ResponseEntity<>("Quiz deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
    }

    // Set quiz visibility (public/private) - owner (or admin) only
    @PutMapping("/{quizId}/visibility")
    public ResponseEntity<Quiz> setQuizVisibility(@PathVariable Long quizId, @RequestParam boolean isPublic, Principal principal) {
        Quiz existing = quizService.getQuizById(quizId).orElse(null);
        if (existing == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

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


    // Student submits answers; scored server-side and the result is saved.
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long quizId,
                                        @RequestBody Map<String, String> answers,
                                        Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        try {
            return ResponseEntity.ok(quizResultService.submit(quizId, principal.getName(), answers));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Gradebook for a quiz - only the owning teacher (or admin).
    @GetMapping("/{quizId}/results")
    public ResponseEntity<?> quizResults(@PathVariable Long quizId, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found");
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Quiz quiz = quizOpt.get();
        boolean owns = quiz.getCreatedBy() != null && user != null
                && user.getEmail().equals(quiz.getCreatedBy().getEmail());
        boolean admin = user != null && user.getRole() == Role.ADMIN;
        if (!owns && !admin) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your quiz");
        return ResponseEntity.ok(quizResultService.resultsForQuiz(quizId));
    }

    // A student's own quiz results.
    @GetMapping("/results/mine")
    public ResponseEntity<?> myResults(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        return ResponseEntity.ok(quizResultService.resultsForStudent(principal.getName()));
    }

    // Quiz for taking - questions WITHOUT the correct answer key.
    @GetMapping("/{quizId}/take")
    public ResponseEntity<?> takeQuiz(@PathVariable Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId).orElse(null);
        if (quiz == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found");
        List<QuizTakeDto.TakeQuestion> questions = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                questions.add(new QuizTakeDto.TakeQuestion(
                        q.getQuestionId(), q.getQuestionText(), q.getQuestionDescription(),
                        q.getAnswers(), q.getPoints()));
            }
        }
        return ResponseEntity.ok(new QuizTakeDto(quiz.getId(), quiz.getName(), quiz.getDescription(), quiz.getDueDate(), questions));
    }
}
