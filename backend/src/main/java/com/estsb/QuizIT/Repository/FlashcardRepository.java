package com.estsb.QuizIT.Repository;

import com.estsb.QuizIT.Entity.Flashcard;
import com.estsb.QuizIT.Entity.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard,Long> {

    List<Flashcard> findByFlashcardSet(FlashcardSet flashcardSet);
}
