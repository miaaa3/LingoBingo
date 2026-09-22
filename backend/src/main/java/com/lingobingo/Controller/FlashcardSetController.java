package com.lingobingo.Controller;


import com.lingobingo.Dto.FlashcardDTO;
import com.lingobingo.Dto.FlashcardResponseDTO;
import com.lingobingo.Dto.FlashcardSetDTO;
import com.lingobingo.Dto.FlashcardSetResponseDTO;
import com.lingobingo.Dto.FlashcardSetJsonDTO;
import com.lingobingo.Entity.ActivityType;
import com.lingobingo.Entity.Flashcard;
import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Entity.Role;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.FlashcardSetService;
import com.lingobingo.Service.RecentActivityService;
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
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/flashcard-sets")
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;
    private final UserRepository userRepository;
    private final RecentActivityService recentActivityService;

    private User currentUser(Principal principal) {
        return (principal != null) ? userRepository.findByEmail(principal.getName()).orElse(null) : null;
    }

    /** Only the set's creator (or an admin) may manage it. */
    private boolean canManage(FlashcardSet set, User user) {
        if (set == null || user == null) return false;
        if (user.getRole() == Role.ADMIN) return true;
        return set.getCreatedBy() != null && user.getId() != null
                && user.getId().equals(set.getCreatedBy().getId());
    }

    // Get all flashcard sets
    @GetMapping("/all")
    public ResponseEntity<List<FlashcardSet>> getAllFlashcardSets() {
        List<FlashcardSet> flashcardSets = flashcardSetService.getAllFlashcardSets();
        return new ResponseEntity<>(flashcardSets, HttpStatus.OK);
    }

    // Toggle public status (true/false) - owner (or admin) only
    @PutMapping("/{id}/public/toggle")
    public ResponseEntity<FlashcardSet> togglePublic(@PathVariable Long id, Principal principal) {
        FlashcardSet existing = flashcardSetService.getFlashcardSetById(id).orElse(null);
        if (existing == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

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
        recentActivityService.log(currentUser(principal), "CREATED", ActivityType.FLASHCARD_SET, createdSet.getTitle());

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


    // Update existing flashcard set - owner (or admin) only
    @PutMapping("/{id}/update")
    public ResponseEntity<FlashcardSet> updateFlashcardSet(@PathVariable Long id, @RequestBody FlashcardSet updatedFlashcardSet, Principal principal) {
        FlashcardSet existing = flashcardSetService.getFlashcardSetById(id).orElse(null);
        if (existing == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        FlashcardSet flashcardSet = flashcardSetService.updateFlashcardSet(id, updatedFlashcardSet);
        if (flashcardSet != null) {
            recentActivityService.log(currentUser(principal), "UPDATED", ActivityType.FLASHCARD_SET, flashcardSet.getTitle());
        }
        return (flashcardSet != null) ?
                new ResponseEntity<>(flashcardSet, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete flashcard set - owner (or admin) only
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFlashcardSet(@PathVariable Long id, Principal principal) {
        FlashcardSet existing = flashcardSetService.getFlashcardSetById(id).orElse(null);
        if (existing == null) return new ResponseEntity<>("FlashcardSet not found", HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>("Not your set", HttpStatus.FORBIDDEN);

        String title = existing.getTitle();
        boolean deleted = flashcardSetService.deleteFlashcardSet(id);
        if (deleted) {
            recentActivityService.log(currentUser(principal), "DELETED", ActivityType.FLASHCARD_SET, title);
        }
        return (deleted) ?
                new ResponseEntity<>("FlashcardSet deleted successfully", HttpStatus.OK) :
                new ResponseEntity<>("FlashcardSet not found", HttpStatus.NOT_FOUND);
    }

    // Set favorite status (true/false) - a personal bookmark, allowed for any signed-in user
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

    // --- Endpoints the frontend expects ---

    // Own + public sets combined (used by the search/flashcards page)
    @GetMapping("/visible")
    public ResponseEntity<Map<String, Object>> getVisibleFlashcardSets(Principal principal) {
        List<FlashcardSet> own = (principal != null)
                ? flashcardSetService.getAllMyFlashcardSets(principal.getName())
                : new ArrayList<>();
        List<FlashcardSet> publicSets = flashcardSetService.getAllPublicFlashcardSets();
        Map<String, Object> body = new HashMap<>();
        body.put("ownSets", own);
        body.put("publicSets", publicSets);
        return ResponseEntity.ok(body);
    }

    // Search own + public sets by name/description
    @GetMapping("/search-separate")
    public ResponseEntity<Map<String, Object>> searchSeparate(
            Principal principal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic) {
        List<FlashcardSet> own = (principal != null)
                ? flashcardSetService.searchMySets(principal.getName(), name, description)
                : new ArrayList<>();
        List<FlashcardSet> publicSets = flashcardSetService.searchPublicSets(name, description);
        Map<String, Object> body = new HashMap<>();
        body.put("ownSets", own);
        body.put("publicSets", publicSets);
        return ResponseEntity.ok(body);
    }

    // Set visibility to an explicit value - owner (or admin) only
    @PutMapping("/{id}/visibility")
    public ResponseEntity<FlashcardSet> setVisibility(@PathVariable Long id, @RequestParam boolean isPublic, Principal principal) {
        FlashcardSet existing = flashcardSetService.getFlashcardSetById(id).orElse(null);
        if (existing == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (!canManage(existing, currentUser(principal))) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        FlashcardSet updated = flashcardSetService.setPublic(id, isPublic);
        return (updated != null)
                ? new ResponseEntity<>(updated, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // JSON create (no media) - complements the multipart /add
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FlashcardSet> addFlashcardSetJson(Principal principal,
                                                            @RequestBody FlashcardSetJsonDTO dto) {
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setTitle(dto.getTitle());
        flashcardSet.setDescription(dto.getDescription());
        if (dto.getFlashcards() != null) {
            List<Flashcard> flashcards = new ArrayList<>();
            for (FlashcardSetJsonDTO.Card c : dto.getFlashcards()) {
                Flashcard flashcard = new Flashcard();
                flashcard.setTerm(c.getTerm());
                flashcard.setDefinition(c.getDefinition());
                flashcard.setFlashcardSet(flashcardSet);
                flashcards.add(flashcard);
            }
            flashcardSet.setFlashcards(flashcards);
        }
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(flashcardSet::setCreatedBy);
        }
        FlashcardSet created = flashcardSetService.createFlashcardSet(flashcardSet);
        recentActivityService.log(currentUser(principal), "CREATED", ActivityType.FLASHCARD_SET, created.getTitle());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
