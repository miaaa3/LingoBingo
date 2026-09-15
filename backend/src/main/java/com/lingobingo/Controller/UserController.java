package com.lingobingo.Controller;

import com.lingobingo.Dto.ProfileDto;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * Current-user profile. Auth via the global /api/** rule. Email is the login
 * identity (JWT subject) and is read-only here; name and username are editable.
 */
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Optional<User> u = userRepository.findByEmail(principal.getName());
        return u.<ResponseEntity<?>>map(user -> ResponseEntity.ok(toDto(user)))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> update(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Optional<User> opt = userRepository.findByEmail(principal.getName());
        if (opt.isEmpty()) return ResponseEntity.status(404).build();
        User user = opt.get();

        String name = body.get("name");
        String username = body.get("username");
        if (name != null && !name.trim().isEmpty()) user.setName(name.trim());
        if (username != null && !username.trim().isEmpty()) user.setUsername(username.trim());
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    private ProfileDto toDto(User u) {
        return new ProfileDto(u.getId(), u.getName(), u.getUsername(), u.getEmail(),
                u.getRole() != null ? u.getRole().name() : null, u.getCreatedAt());
    }
}
