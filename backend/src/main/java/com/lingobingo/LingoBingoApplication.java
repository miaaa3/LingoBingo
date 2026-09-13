package com.lingobingo;

import com.lingobingo.Dto.RegisterRequest;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Service.Authentication.AuthenticationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LingoBingoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LingoBingoApplication.class, args);
	}
	@Bean
	CommandLineRunner run(UserRepository userRepository, AuthenticationService authenticationService) {
		return args -> {
			if (userRepository.findByEmail("&").isEmpty()) {
				RegisterRequest registerRequest = new RegisterRequest();
				registerRequest.setEmail("admin@lingobingo.com");
				registerRequest.setUsername("admin");
				registerRequest.setPassword("password123");

				authenticationService.register(registerRequest);

				System.out.println("✅ Default admin user created: admin@lingobingo.com");
			} else {
				System.out.println("ℹ️ Admin user already exists.");
			}
		};
	}
}