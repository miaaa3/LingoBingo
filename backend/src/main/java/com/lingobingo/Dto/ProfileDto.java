package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Safe view of the signed-in user (no password). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
