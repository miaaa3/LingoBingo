package com.lingobingo.Controller;

import com.lingobingo.Dto.CreatePlanningEventRequest;
import com.lingobingo.Dto.PlanningEventDto;
import com.lingobingo.Entity.PlanningEvent;
import com.lingobingo.Entity.Role;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.PlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(Principal principal, @RequestBody CreatePlanningEventRequest request) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        if (!isTeacher(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only teachers can schedule events");

        LocalDateTime due;
        try {
            due = (request.getDueDate() != null) ? LocalDateTime.parse(request.getDueDate()) : null;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date/time");
        }
        try {
            PlanningEvent event = planningService.create(user.getEmail(), request.getClassId(),
                    request.getTitle(), request.getDescription(), request.getType(), due);
            return ResponseEntity.ok(toDto(event));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(Principal principal) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        var events = isTeacher(user)
                ? planningService.forTeacher(user.getEmail())
                : planningService.forStudent(user.getEmail());
        return ResponseEntity.ok(events.stream().map(this::toDto).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Principal principal, @PathVariable Long id) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        try {
            boolean deleted = planningService.delete(id, user.getEmail());
            return deleted ? ResponseEntity.ok("Deleted")
                           : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your event");
        }
    }

    private User currentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    private boolean isTeacher(User user) {
        return user.getRole() == Role.TEACHER || user.getRole() == Role.ADMIN;
    }

    private PlanningEventDto toDto(PlanningEvent e) {
        Long classId = (e.getClassroom() != null) ? e.getClassroom().getId() : null;
        String className = (e.getClassroom() != null) ? e.getClassroom().getName() : null;
        return new PlanningEventDto(e.getId(), e.getTitle(), e.getDescription(),
                e.getType(), e.getDueDate(), classId, className);
    }
}
