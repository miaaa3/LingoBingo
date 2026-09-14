package com.lingobingo.Controller;

import com.lingobingo.Entity.PracticeSession;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.PracticeSessionRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Records a finished practice session so its XP counts toward the dashboard
 * (points, level, streak). Auth via the global /api/** rule.
 */
@RestController
@RequestMapping("api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeSessionRepository practiceSessionRepository;
    private final UserRepository userRepository;

    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestBody Map<String, Object> body, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        PracticeSession s = new PracticeSession();
        s.setStudent(userOpt.get());
        s.setXp(clampNonNeg(body.get("xp")));
        s.setCorrect(clampNonNeg(body.get("correct")));
        s.setTotal(clampNonNeg(body.get("total")));
        s.setSubmittedAt(LocalDateTime.now());
        practiceSessionRepository.save(s);
        return ResponseEntity.ok(Map.of("ok", true, "xp", s.getXp()));
    }

    private int clampNonNeg(Object v) {
        if (v instanceof Number n) return Math.max(0, n.intValue());
        try { return Math.max(0, Integer.parseInt(String.valueOf(v))); }
        catch (Exception e) { return 0; }
    }
}
