package com.estsb.QuizIT.Dto;

import com.estsb.QuizIT.Entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGameResponse {
    private String gameCode;
    private Game game;
}
