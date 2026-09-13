package com.lingobingo.Controller;

import com.lingobingo.Dto.AssignContentRequest;
import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.ClassContent;
import com.lingobingo.Entity.Role;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.ClassroomRepository;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.ClassContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class-content")
public class ClassContentController {

    private final ClassContentService classContentService;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @PostMapping("/assign")
    public ResponseEntity<?> assign(Principal principal, @RequestBody AssignContentRequest request) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        if (!isTeacher(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only teachers can assign content");
        try {
            ClassContent cc = classContentService.assign(user.getEmail(), request.getClassId(),
                    request.getContentType(), request.getContentId());
            if (cc == null) return ResponseEntity.badRequest().body("Already assigned to this class");
            return ResponseEntity.ok(classContentService.forClass(request.getClassId()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> unassign(Principal principal, @PathVariable Long id) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        try {
            boolean removed = classContentService.unassign(id, user.getEmail());
            return removed ? ResponseEntity.ok("Removed")
                           : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your class");
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> forClass(Principal principal, @PathVariable Long classId) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        Classroom classroom = classroomRepository.findById(classId).orElse(null);
        if (classroom == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Class not found");
        boolean owns = classroom.getTeacher() != null && user.getEmail().equals(classroom.getTeacher().getEmail());
        boolean enrolled = enrollmentRepository.existsByClassroomIdAndStudentEmail(classId, user.getEmail());
        boolean admin = user.getRole() == Role.ADMIN;
        if (!owns && !enrolled && !admin) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this class");
        return ResponseEntity.ok(classContentService.forClass(classId));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(Principal principal) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        return ResponseEntity.ok(classContentService.forStudent(user.getEmail()));
    }

    private User currentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    private boolean isTeacher(User user) {
        return user.getRole() == Role.TEACHER || user.getRole() == Role.ADMIN;
    }
}
