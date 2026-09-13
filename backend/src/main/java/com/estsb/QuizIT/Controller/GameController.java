package com.estsb.QuizIT.Controller;

import com.estsb.QuizIT.Dto.QuestionDTO;
import com.estsb.QuizIT.Entity.Game;
import com.estsb.QuizIT.Entity.Player;
import com.estsb.QuizIT.Entity.User;
import com.estsb.QuizIT.Repository.UserRepository;
import com.estsb.QuizIT.Service.GameService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/game")
public class GameController {

    private final GameService gameService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Game> createGame(
            @RequestParam String questionType,
            @RequestParam int numberOfQuestions,
            @RequestParam(required = false) Long flashcardSetId,
            @RequestParam(required = false) Long quizId,
            Principal principal) {

        String teacherUsername = principal.getName();
        User teacher = userRepository.findByEmail(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Game newGame = gameService.createGame(questionType, numberOfQuestions, flashcardSetId, quizId, teacher.getId());

        return ResponseEntity.ok(newGame);
    }

    @GetMapping(value = "/{gameCode}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getGameQRCode(@PathVariable String gameCode) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // Frontend URL
        String frontendBaseUrl = "http://10.192.138.232:4200";

        // Full URL encoded in the QR code — directs to JoinGameComponent route
        String qrContent = frontendBaseUrl + "/choose-profile/" + gameCode ;

        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(pngData);
    }


    @PostMapping("/join")
    public ResponseEntity<?> joinGame(
            @RequestParam String gameCode,
            @RequestParam String username,
            @RequestParam String avatarUrl) {

        Player player = gameService.joinGame(gameCode, username, avatarUrl);
        if (player != null) {
            // Return player info (including ID) as JSON
            return ResponseEntity.ok(player);
        } else {
            return ResponseEntity.badRequest().body("Failed to join: invalid code or already joined.");
        }
    }


    @PostMapping("/start/{gameId}")
    public ResponseEntity<String> startGame(@PathVariable Long gameId) {
        boolean started = gameService.startGame(gameId);
        if (started) {
            return ResponseEntity.ok("Game started!");
        } else {
            return ResponseEntity.badRequest().body("Cannot start game: not enough players.");
        }
    }

    @PostMapping("/check/{gameId}")
    public ResponseEntity<String> checkAnswer(
            @PathVariable Long gameId,
            @RequestParam Long questionId,
            @RequestParam String answer,
            @RequestParam Long playerId) {

        String result = gameService.checkAnswer(gameId, questionId, answer, playerId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/finish/{gameId}/{playerId}")
    public ResponseEntity<String> finishGame(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {

        String result = gameService.finishGame(gameId, playerId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/end/{gameId}")
    public ResponseEntity<String> endGame(@PathVariable Long gameId) {
        String leaderboard = gameService.endGame(gameId);

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Game>> getActiveGames() {
        List<Game> activeGames = gameService.getActiveGames();
        return ResponseEntity.ok(activeGames);
    }

    @GetMapping("/byCode/{gameCode}")
    public ResponseEntity<Game> getGameByCode(@PathVariable String gameCode) {
        Game game = gameService.getGameByCode(gameCode);
        return ResponseEntity.ok(game);
    }

    @GetMapping("/createdBy/{gameCode}")
    public ResponseEntity<User> getCreatedBy(@PathVariable String gameCode) {
        Game game = gameService.getGameByCode(gameCode);

        return ResponseEntity.ok(game.getCreatedBy());
    }

    @GetMapping("/players/{gameId}")
    public ResponseEntity<List<Player>> getPlayersForGame(@PathVariable Long gameId) {
        List<Player> players = gameService.getPlayersForGame(gameId);
        return ResponseEntity.ok(players);
    }

    @GetMapping("/game-questions/{gameCode}")
    public ResponseEntity<List<QuestionDTO>> getGameQuestions(@PathVariable String gameCode) {
        Game game = gameService.getGameByCode(gameCode);

        // Generate questions based on whether it's a flashcard or quiz game
        List<QuestionDTO> questions = gameService.generateQuestions(game);

        return ResponseEntity.ok(questions);
    }

    @PostMapping("/updateScore/{gameId}")
    public ResponseEntity<String> updateScore(
            @PathVariable Long gameId,
            @RequestParam Long playerId) {
        try {
            gameService.updateScore(gameId, playerId);
            return ResponseEntity.ok("Player score updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating score: " + e.getMessage());
        }
    }

}
