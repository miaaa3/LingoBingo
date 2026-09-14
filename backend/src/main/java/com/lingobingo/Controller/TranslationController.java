package com.lingobingo.Controller;

import com.lingobingo.Service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin translation endpoint used by the flashcard authoring helper and the
 * language practice screens. Auth is handled by the global /api/** rule.
 */
@RestController
@RequestMapping("api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> translate(
            @RequestParam("q") String q,
            @RequestParam(value = "from", defaultValue = "en") String from,
            @RequestParam(value = "to", defaultValue = "fr") String to) {

        String translation = translationService.translate(q, from, to);
        Map<String, Object> body = new HashMap<>();
        body.put("query", q);
        body.put("from", from);
        body.put("to", to);
        body.put("translation", translation == null ? "" : translation);
        body.put("ok", translation != null);
        return ResponseEntity.ok(body);
    }
}
