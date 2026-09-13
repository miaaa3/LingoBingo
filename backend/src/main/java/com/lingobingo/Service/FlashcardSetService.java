package com.lingobingo.Service;

import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardService flashcardService;

    // Get all flashcard sets
    public List<FlashcardSet> getAllFlashcardSets() {
        return flashcardSetRepository.findAll();
    }

    // Get flashcard set by ID
    public Optional<FlashcardSet> getFlashcardSetById(Long id) {
        return flashcardSetRepository.findById(id);
    }

    // Create a new flashcard set
    public FlashcardSet createFlashcardSet(FlashcardSet flashcardSet) {
        if (flashcardSet.getFlashcards() != null) {
            flashcardSet.getFlashcards().forEach(flashcard -> flashcard.setFlashcardSet(flashcardSet));
        }
        return flashcardSetRepository.save(flashcardSet);
    }

    // Update an existing flashcard set
    public FlashcardSet updateFlashcardSet(Long id, FlashcardSet updatedFlashcardSet) {
        if (flashcardSetRepository.existsById(id)) {
            updatedFlashcardSet.setId(id);
            if (updatedFlashcardSet.getFlashcards() != null) {
                updatedFlashcardSet.getFlashcards().forEach(flashcard -> flashcard.setFlashcardSet(updatedFlashcardSet));
            }
            return flashcardSetRepository.save(updatedFlashcardSet);
        }
        return null;
    }

    // Delete flashcard set by ID
    public boolean deleteFlashcardSet(Long id) {
        if (flashcardSetRepository.existsById(id)) {
            flashcardSetRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Mark or unmark flashcard set as favorite
    public FlashcardSet setFavorite(Long id, boolean favorite) {
        Optional<FlashcardSet> optionalSet = flashcardSetRepository.findById(id);
        if (optionalSet.isPresent()) {
            FlashcardSet flashcardSet = optionalSet.get();
            flashcardSet.setIsFavorite(favorite); // set favorite field
            return flashcardSetRepository.save(flashcardSet);
        }
        return null;
    }

    // Toggle favorite status
    public FlashcardSet toggleFavorite(Long id) {
        Optional<FlashcardSet> optionalSet = flashcardSetRepository.findById(id);
        if (optionalSet.isPresent()) {
            FlashcardSet flashcardSet = optionalSet.get();
            flashcardSet.setIsFavorite(!Boolean.TRUE.equals(flashcardSet.getIsFavorite())); // flip value
            return flashcardSetRepository.save(flashcardSet);
        }
        return null;
    }

    // Toggle the 'isPublic' field of a flashcard set
    public FlashcardSet togglePublic(Long id) {
        Optional<FlashcardSet> optionalSet = flashcardSetRepository.findById(id);
        if (optionalSet.isPresent()) {
            FlashcardSet flashcardSet = optionalSet.get();
            flashcardSet.setIsPublic(!Boolean.TRUE.equals(flashcardSet.getIsPublic())); // flip value
            return flashcardSetRepository.save(flashcardSet);
        }
        return null;
    }

    // Get all flashcard sets that are public
    public List<FlashcardSet> getAllPublicFlashcardSets() {
        return flashcardSetRepository.findAllByIsPublicTrue();
    }

    // Get all flashcard sets created by a specific user
    public List<FlashcardSet> getAllMyFlashcardSets(String userEmail) {
        return flashcardSetRepository.findAllByCreatedByEmail(userEmail);
    }

    // Set visibility to an explicit value
    public FlashcardSet setPublic(Long id, boolean isPublic) {
        Optional<FlashcardSet> optionalSet = flashcardSetRepository.findById(id);
        if (optionalSet.isPresent()) {
            FlashcardSet flashcardSet = optionalSet.get();
            flashcardSet.setIsPublic(isPublic);
            return flashcardSetRepository.save(flashcardSet);
        }
        return null;
    }

    // Search helpers (small dataset - filtered in memory)
    public List<FlashcardSet> searchMySets(String email, String name, String description) {
        return filter(getAllMyFlashcardSets(email), name, description);
    }

    public List<FlashcardSet> searchPublicSets(String name, String description) {
        return filter(getAllPublicFlashcardSets(), name, description);
    }

    private List<FlashcardSet> filter(List<FlashcardSet> sets, String name, String description) {
        return sets.stream()
                .filter(s -> name == null || name.isBlank()
                        || (s.getTitle() != null && s.getTitle().toLowerCase().contains(name.toLowerCase())))
                .filter(s -> description == null || description.isBlank()
                        || (s.getDescription() != null && s.getDescription().toLowerCase().contains(description.toLowerCase())))
                .collect(Collectors.toList());
    }
}
