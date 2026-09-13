package com.estsb.QuizIT.Service.Authentication;


import com.estsb.QuizIT.Dto.AuthenticationRequest;
import com.estsb.QuizIT.Dto.AuthenticationResponse;
import com.estsb.QuizIT.Dto.RegisterRequest;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists.");
        } else if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this username already exists.");
        }
        User createdUser = new User(registerRequest.getEmail(),
                                    registerRequest.getUsername(),
                                    passwordEncoder.encode(registerRequest.getPassword()),
                                    "USER");

        userRepository.save(createdUser);

        String jwtAccessToken = jwtService.generateToken(createdUser);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .email(createdUser.getEmail())
                .access_token(jwtAccessToken)
                .role(createdUser.getRole()).build());
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest) {
        try {
            User user = userRepository.findByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("USER not found"));

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwtAccessToken = jwtService.generateToken(user);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .access_token(jwtAccessToken)
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole()).build());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid credential: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
