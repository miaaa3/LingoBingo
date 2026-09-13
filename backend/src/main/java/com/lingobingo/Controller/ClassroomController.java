package com.lingobingo.Controller;

import com.lingobingo.Dto.ClassroomDto;
import com.lingobingo.Dto.CreateClassRequest;
import com.lingobingo.Dto.JoinClassRequest;
import com.lingobingo.Dto.MemberResponse;
import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Entity.Role;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final UserRepository userRepository;

    // Teacher (or admin) creates a class and gets its join code.
    @PostMapping("/create")
    public ResponseEntity<?> create(Principal principal, @RequestBody CreateClassRequest request) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        if (!isTeacher(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only teachers can create classes");
        }
        Classroom created = classroomService.createClass(user.getEmail(), request.getName(), request.getDescription());
        return ResponseEntity.ok(toDto(created));
    }

    // Student joins a class using its code.
    @PostMapping("/join")
    public ResponseEntity<?> join(Principal principal, @RequestBody JoinClassRequest request) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        try {
            Enrollment enrollment = classroomService.joinClass(user.getEmail(), request.getJoinCode());
            if (enrollment == null) {
                return ResponseEntity.badRequest().body("You are already enrolled in this class");
            }
            return ResponseEntity.ok(toDto(enrollment.getClassroom()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Classes the caller owns (teacher) or is enrolled in (student).
    @GetMapping("/mine")
    public ResponseEntity<?> mine(Principal principal) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        List<Classroom> classes = isTeacher(user)
                ? classroomService.getClassesForTeacher(user.getEmail())
                : classroomService.getClassesForStudent(user.getEmail());
        return ResponseEntity.ok(classes.stream().map(this::toDto).toList());
    }

    // Roster - only the owning teacher (or admin) can see it.
    @GetMapping("/{id}/members")
    public ResponseEntity<?> members(Principal principal, @PathVariable Long id) {
        User user = currentUser(principal);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        Classroom classroom = classroomService.getById(id).orElse(null);
        if (classroom == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Class not found");
        boolean owns = classroom.getTeacher() != null && user.getEmail().equals(classroom.getTeacher().getEmail());
        if (user.getRole() != Role.ADMIN && !owns) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your class");
        }
        List<MemberResponse> members = classroomService.getMembers(id).stream()
                .map(m -> new MemberResponse(m.getId(), m.getName(), m.getEmail()))
                .toList();
        return ResponseEntity.ok(members);
    }

    private User currentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    private boolean isTeacher(User user) {
        return user.getRole() == Role.TEACHER || user.getRole() == Role.ADMIN;
    }

    private ClassroomDto toDto(Classroom c) {
        String teacherName = (c.getTeacher() != null) ? c.getTeacher().getName() : null;
        return new ClassroomDto(c.getId(), c.getName(), c.getDescription(), c.getJoinCode(),
                teacherName, classroomService.memberCount(c.getId()), c.getCreatedAt());
    }
}
