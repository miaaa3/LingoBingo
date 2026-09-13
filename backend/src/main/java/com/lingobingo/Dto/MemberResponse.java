package com.lingobingo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
}
