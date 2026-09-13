package com.lingobingo.Dto;

import com.lingobingo.Entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGameResponse {
    private String gameCode;
    private Game game;
}
