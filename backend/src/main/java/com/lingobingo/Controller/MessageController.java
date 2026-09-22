package com.lingobingo.Controller;

import com.lingobingo.Service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService service;

    @GetMapping("/contacts")
    public ResponseEntity<?> contacts(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(service.contacts(principal.getName()));
    }

    @GetMapping("/threads")
    public ResponseEntity<?> threads(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(service.threads(principal.getName()));
    }

    @GetMapping("/with/{userId}")
    public ResponseEntity<?> thread(Principal principal, @PathVariable Long userId) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(service.thread(principal.getName(), userId));
    }

    @PostMapping
    public ResponseEntity<?> send(Principal principal, @RequestBody Map<String, Object> body) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        Object rid = body.get("recipientId");
        Object content = body.get("content");
        if (!(rid instanceof Number) || content == null) {
            return ResponseEntity.badRequest().body("recipientId and content are required");
        }
        service.send(principal.getName(), ((Number) rid).longValue(), content.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unread(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(Map.of("count", service.unreadCount(principal.getName())));
    }
}
