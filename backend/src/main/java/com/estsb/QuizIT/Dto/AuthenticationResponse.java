package com.estsb.QuizIT.Dto;

import com.estsb.QuizIT.Entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private Long id;
    private String access_token;
    private String email;
    private Role role;

}