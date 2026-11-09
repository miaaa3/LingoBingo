package com.lingobingo.Controller;

import com.lingobingo.Entity.Flashcard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BingoControllerTest {

    private static Flashcard card(String term, String def) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition(def);
        return f;
    }

    @Test
    void duplicateAndBlankTermsAreMergedSoSquaresStayDistinct() {
        var words = BingoController.distinctWords(List.of(
                card("Apple", "pomme"),
                card("  apple ", "pomme (bis)"),
                card("", "vide"),
                card("ice  cream", "glace"),
                card("Ice cream", "glace"),
                card("pear", "poire")));
        assertEquals(List.of("Apple", "ice  cream", "pear"),
                words.stream().map(BingoController.Word::term).toList());
    }
}
