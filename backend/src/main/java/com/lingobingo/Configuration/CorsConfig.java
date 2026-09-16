package com.lingobingo.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow any origin so the app works whether it's opened via localhost or a
        // LAN IP (e.g. from a phone on the same Wi-Fi). Auth uses a Bearer token in
        // the Authorization header (not cookies), so allowing all origins is safe;
        // allowedOriginPatterns("*") is required because "*" is not permitted
        // together with allowCredentials(true).
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
