package com.estsb.QuizIT.Controller;

import com.estsb.QuizIT.Entity.Flashcard;
import com.estsb.QuizIT.Service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @GetMapping("/all")
    public ResponseEntity<List<Flashcard>> getAllFlashcards() {
        List<Flashcard> flashcards = flashcardService.getAllFlashcards();
        return new ResponseEntity<>(flashcards, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getFlashcardById(@PathVariable Long id) {
        Optional<Flashcard> flashcard = flashcardService.getFlashcardById(id);
        return flashcard.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/add")
    public ResponseEntity<Flashcard> addFlashcard(@RequestBody Flashcard flashcard) {
        Flashcard createdFlashcard = flashcardService.createFlashcard(flashcard);
        return new ResponseEntity<>(createdFlashcard, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Flashcard> updateFlashcard(@PathVariable Long id, @RequestBody Flashcard updatedFlashcard) {
        Flashcard flashcard = flashcardService.updateFlashcard(id, updatedFlashcard);
        return (flashcard != null) ?
                new ResponseEntity<>(flashcard, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFlashcard(@PathVariable Long id) {
        boolean deleted = flashcardService.deleteFlashcard(id);
        return (deleted) ?
                new ResponseEntity<>("Flashcard deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("Flashcard not found", HttpStatus.NOT_FOUND);
    }
}
