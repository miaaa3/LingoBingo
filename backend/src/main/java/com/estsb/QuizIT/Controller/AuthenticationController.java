package com.estsb.QuizIT.Controller;


import com.estsb.QuizIT.Dto.AuthenticationRequest;
import com.estsb.QuizIT.Dto.RegisterRequest;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.UserRepository;
import com.estsb.QuizIT.Service.Authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        return authenticationService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticationService.authenticate(authenticationRequest);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAuthenticatedUser(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Optional<User> authenticatedUser = userRepository.findByEmail(username);
            return ResponseEntity.ok(authenticatedUser);
        } else {
            return ResponseEntity.status(401).body("User not authenticated");
        }
    }
}
