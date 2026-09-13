package com.lingobingo.Dto;

import lombok.Data;
import java.util.List;

@Data
public class FlashcardSetJsonDTO {
    private String title;
    private String description;
    private List<Card> flashcards;

    @Data
    public static class Card {
        private String term;
        private String definition;
    }
}
