package com.estsb.QuizIT.Service;

import com.estsb.QuizIT.Entity.Flashcard;
import com.estsb.QuizIT.Repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;


    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    public Optional<Flashcard> getFlashcardById(Long id) {
        return flashcardRepository.findById(id);
    }

    public Flashcard createFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }

    public Flashcard updateFlashcard(Long id, Flashcard updatedFlashcard) {
        if (flashcardRepository.existsById(id)) {
            updatedFlashcard.setId(id);
            return flashcardRepository.save(updatedFlashcard);
        }
        return null;
    }

    public boolean deleteFlashcard(Long id) {
        if (flashcardRepository.existsById(id)) {
            flashcardRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
