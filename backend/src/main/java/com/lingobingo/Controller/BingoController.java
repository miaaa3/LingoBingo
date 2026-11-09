package com.lingobingo.Controller;

import com.lingobingo.Entity.Flashcard;
import com.lingobingo.Entity.FlashcardSet;
import com.lingobingo.Service.BingoService;
import com.lingobingo.Service.FlashcardSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Vocabulary bingo for the classroom. Stateless: the teacher's browser keeps
 * the generated grids; the server only builds them and referees wins.
 * Auth via the global /api/** rule.
 */
@RestController
@RequestMapping("api/bingo")
@RequiredArgsConstructor
public class BingoController {

    private final BingoService bingoService;
    private final FlashcardSetService flashcardSetService;

    public record GenerateRequest(Long setId, Integer size, Integer count, Boolean freeCenter) {}
    public record Word(String term, String definition) {}
    public record GenerateResponse(String setTitle, int size, List<Word> words, List<int[]> grids, String uniqueBy) {}
    public record CheckRequest(int[] grid, List<Integer> called) {}

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateRequest req) {
        if (req == null || req.setId() == null)
            return badRequest("Choose a flashcard set.");
        Optional<FlashcardSet> setOpt = flashcardSetService.getFlashcardSetById(req.setId());
        if (setOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<Word> words = distinctWords(setOpt.get().getFlashcards());
        int size = req.size() == null ? 4 : req.size();
        int count = req.count() == null ? 25 : req.count();
        try {
            BingoService.GenerationResult r = bingoService.generate(
                    words.size(), size, count, Boolean.TRUE.equals(req.freeCenter()));
            return ResponseEntity.ok(new GenerateResponse(setOpt.get().getTitle(), size, words, r.grids(), r.uniqueBy()));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody CheckRequest req) {
        if (req == null || req.grid() == null) return badRequest("Missing grid.");
        try {
            return ResponseEntity.ok(bingoService.check(req.grid(),
                    req.called() == null ? List.of() : req.called()));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * One entry per distinct term (case/space-insensitive): two flashcards with
     * the same word would otherwise look like two different squares.
     */
    static List<Word> distinctWords(List<Flashcard> cards) {
        Map<String, Word> byKey = new LinkedHashMap<>();
        if (cards != null) {
            for (Flashcard c : cards) {
                String term = c.getTerm() == null ? "" : c.getTerm().trim();
                if (term.isEmpty()) continue;
                String def = c.getDefinition() == null ? "" : c.getDefinition().trim();
                byKey.putIfAbsent(term.toLowerCase(Locale.ROOT).replaceAll("\\s+", " "), new Word(term, def));
            }
        }
        return new ArrayList<>(byKey.values());
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }
}
