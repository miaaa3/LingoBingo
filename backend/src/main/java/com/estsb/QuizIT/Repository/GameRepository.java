package com.estsb.QuizIT.Repository;

import com.estsb.QuizIT.Entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game,Long> {
    Optional<Game> findById(Long gameId);

    List<Game> findByIsActiveTrue();

    Optional<Game> findByGameCode(String gameCode);
}
