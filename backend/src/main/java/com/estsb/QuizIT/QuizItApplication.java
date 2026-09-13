package com.estsb.QuizIT;

import com.estsb.QuizIT.Dto.RegisterRequest;
import com.estsb.QuizIT.Repository.UserRepository;
import com.estsb.QuizIT.Service.Authentication.AuthenticationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuizItApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizItApplication.class, args);
	}
	@Bean
	CommandLineRunner run(UserRepository userRepository, AuthenticationService authenticationService) {
		return args -> {
			if (userRepository.findByEmail("&").isEmpty()) {
				RegisterRequest registerRequest = new RegisterRequest();
				registerRequest.setEmail("admin@quizit.com");
				registerRequest.setUsername("admin");
				registerRequest.setPassword("password123");

				authenticationService.register(registerRequest);

				System.out.println("✅ Default admin user created: admin@quizit.com");
			} else {
				System.out.println("ℹ️ Admin user already exists.");
			}
		};
	}
}