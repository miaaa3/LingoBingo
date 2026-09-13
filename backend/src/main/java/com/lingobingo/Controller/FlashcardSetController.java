package com.lingobingo.Controller;


import com.lingobingo.Dto.FlashcardDTO;
import com.lingobingo.Dto.FlashcardResponseDTO;
import com.lingobingo.Dto.FlashcardSetDTO;
import com.lingobingo.Dto.FlashcardSetResponseDTO;
import com.lingobingo.Entity.Flashcard;
import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.FlashcardSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/flashcard-sets")
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;
    private final UserRepository userRepository;

    // Get all flashcard sets
    @GetMapping("/all")
    public ResponseEntity<List<FlashcardSet>> getAllFlashcardSets() {
        List<FlashcardSet> flashcardSets = flashcardSetService.getAllFlashcardSets();
        return new ResponseEntity<>(flashcardSets, HttpStatus.OK);
    }

    // Toggle public status (true/false)
    @PutMapping("/{id}/public/toggle")
    public ResponseEntity<FlashcardSet> togglePublic(@PathVariable Long id) {
        FlashcardSet updatedSet = flashcardSetService.togglePublic(id);
        return (updatedSet != null) ?
                new ResponseEntity<>(updatedSet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get all public flashcard sets
    @GetMapping("/public")
    public ResponseEntity<List<FlashcardSet>> getAllPublicFlashcardSets() {
        List<FlashcardSet> publicSets = flashcardSetService.getAllPublicFlashcardSets();
        return new ResponseEntity<>(publicSets, HttpStatus.OK);
    }

    // Get all flashcard sets of the currently logged-in user
    @GetMapping("/my-sets")
    public ResponseEntity<List<FlashcardSet>> getAllMyFlashcardSets(Principal principal) {
        if (principal == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<FlashcardSet> mySets = flashcardSetService.getAllMyFlashcardSets(principal.getName());
        return new ResponseEntity<>(mySets, HttpStatus.OK);
    }


    // Get flashcard set by ID
    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSet> getFlashcardSetById(@PathVariable Long id) {
        Optional<FlashcardSet> flashcardSet = flashcardSetService.getFlashcardSetById(id);
        return flashcardSet.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Add new flashcard set (assigns createdBy if authenticated)
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FlashcardSetResponseDTO> addFlashcardSet(
            Principal principal,
            @ModelAttribute FlashcardSetDTO flashcardSetDTO) throws IOException {

        // Create a new FlashcardSet entity
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setTitle(flashcardSetDTO.getTitle());
        flashcardSet.setDescription(flashcardSetDTO.getDescription());

        // Prepare list for flashcard responses (without raw media bytes)
        List<FlashcardResponseDTO> flashcardResponses = new ArrayList<>();
        if (flashcardSetDTO.getFlashcards() != null) {
            List<Flashcard> flashcards = new ArrayList<>();
            for (FlashcardDTO fcDto : flashcardSetDTO.getFlashcards()) {
                // Create new Flashcard entity
                Flashcard flashcard = new Flashcard();
                flashcard.setTerm(fcDto.getTerm());
                flashcard.setDefinition(fcDto.getDefinition());

                // Handle media if provided
                if (fcDto.getMedia() != null && !fcDto.getMedia().isEmpty()) {
                    flashcard.setMedia(fcDto.getMedia().getBytes()); // Save file as byte[]
                    flashcard.setMediaFilename(fcDto.getMedia().getOriginalFilename()); // Save original file name
                    flashcard.setMediaType(fcDto.getMedia().getContentType()); // Save MIME type
                }

                flashcard.setFlashcardSet(flashcardSet); // Link flashcard to set
                flashcards.add(flashcard);

                // Create response DTO without media byte[] (only metadata)
                flashcardResponses.add(new FlashcardResponseDTO(
                        flashcard.getTerm(),
                        flashcard.getDefinition(),
                        flashcard.getMediaFilename(),
                        flashcard.getMediaType()
                ));
            }
            flashcardSet.setFlashcards(flashcards);
        }

        // Set current user as creator if logged in
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(flashcardSet::setCreatedBy);
        }

        // Save flashcard set via service
        FlashcardSet createdSet = flashcardSetService.createFlashcardSet(flashcardSet);

        // Prepare response DTO with flashcards (without raw media)
        FlashcardSetResponseDTO responseDTO = new FlashcardSetResponseDTO(
                createdSet.getId(),
                createdSet.getTitle(),
                createdSet.getDescription(),
                flashcardResponses
        );

        // Return HTTP 201 (Created) with response DTO
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    // Update existing flashcard set
    @PutMapping("/{id}/update")
    public ResponseEntity<FlashcardSet> updateFlashcardSet(@PathVariable Long id, @RequestBody FlashcardSet updatedFlashcardSet) {
        FlashcardSet flashcardSet = flashcardSetService.updateFlashcardSet(id, updatedFlashcardSet);
        return (flashcardSet != null) ?
                new ResponseEntity<>(flashcardSet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete flashcard set
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFlashcardSet(@PathVariable Long id) {
        boolean deleted = flashcardSetService.deleteFlashcardSet(id);
        return (deleted) ?
                new ResponseEntity<>("FlashcardSet deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("FlashcardSet not found", HttpStatus.NOT_FOUND);
    }

    // Set favorite status (true/false)
    @PutMapping("/{id}/favorite/{status}")
    public ResponseEntity<FlashcardSet> setFavorite(@PathVariable Long id, @PathVariable boolean status) {
        FlashcardSet updatedSet = flashcardSetService.setFavorite(id, status);
        return (updatedSet != null) ?
                new ResponseEntity<>(updatedSet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Toggle favorite status
    @PutMapping("/{id}/favorite/toggle")
    public ResponseEntity<FlashcardSet> toggleFavorite(@PathVariable Long id) {
        FlashcardSet updatedSet = flashcardSetService.toggleFavorite(id);
        return (updatedSet != null) ?
                new ResponseEntity<>(updatedSet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
