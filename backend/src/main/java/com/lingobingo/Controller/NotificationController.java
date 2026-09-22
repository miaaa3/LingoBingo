package com.lingobingo.Controller;

import com.lingobingo.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    // Current user's latest notifications.
    @GetMapping
    public ResponseEntity<?> mine(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(service.forUser(principal.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unread(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        return ResponseEntity.ok(Map.of("count", service.unreadCount(principal.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> read(Principal principal, @PathVariable Long id) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        service.markRead(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> readAll(Principal principal) {
        if (principal == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        service.markAllRead(principal.getName());
        return ResponseEntity.ok().build();
    }
}
