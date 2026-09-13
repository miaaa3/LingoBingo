package com.estsb.QuizIT.Service;

import com.estsb.QuizIT.Dto.QuestionDTO;
import com.estsb.QuizIT.Entity.*;
import com.estsb.QuizIT.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final PlayerRepository playerRepository;

    public String generateQRCodeBase64(String content) throws Exception {
        byte[] qrBytes = qrCodeService.generateQRCodeByteArray(content);
        return java.util.Base64.getEncoder().encodeToString(qrBytes);
    }

    public Game createGame(String questionType, int numberOfQuestions, Long flashcardSetId, Long quizId, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Game newGame = new Game();
        newGame.setPlayers(new ArrayList<>());
        newGame.setCreatedBy(teacher);
        newGame.setQuestionType(QuestionType.valueOf(questionType.toUpperCase()));
        newGame.setNumberOfQuestions(numberOfQuestions);
        newGame.setIsActive(false);

        if (flashcardSetId != null) {
            FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                    .orElseThrow(() -> new IllegalArgumentException("Flashcard set not found"));
            newGame.setFlashcardSet(flashcardSet);
        }

        if (quizId != null) {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
            newGame.setQuiz(quiz);
        }

        String gameCode = generateGameCode();
        newGame.setGameCode(gameCode);

        // Initialize score and finish time maps to avoid NullPointerException later
        newGame.setPlayerScores(new HashMap<>());
        newGame.setPlayerFinishTimes(new HashMap<>());

        gameRepository.save(newGame);
        return newGame;
    }

    public Player joinGame(String gameCode, String username, String avatarUrl) {
        Game game = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player = playerRepository.findByUsername(username).orElse(null);

        if (player == null) {
            player = new Player();
            player.setUsername(username);
            player.setAvatarUrl(avatarUrl);
            player = playerRepository.save(player);
        }

        Player finalPlayer = player;
        boolean alreadyJoined = game.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(finalPlayer.getId()));

        if (alreadyJoined) {
            return null;  // or throw an exception or handle as you prefer
        }

        game.getPlayers().add(player);
        gameRepository.save(game);

        return player;
    }


    private String generateGameCode() {
        Random random = new Random();
        return "GAME-" + (random.nextInt(900000) + 100000); // 6-digit code, padded
    }

    public boolean startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getPlayers().isEmpty()) {
            return false; // Minimum 1 player required
        }

        game.setIsActive(true);
        gameRepository.save(game);
        return true;
    }

    /**
     * Génère une liste de questions (DTO) aléatoires pour la partie,
     * soit à partir des flashcards, soit à partir des questions du quiz.
     */
    public List<QuestionDTO> generateQuestions(Game game) {
        List<QuestionDTO> questions = new ArrayList<>();
        Random random = new Random();

        boolean isFlashcardGame = game.getFlashcardSet() != null;
        boolean isQuizGame = game.getQuiz() != null;

        if (isFlashcardGame) {
            List<Flashcard> allFlashcards = flashcardRepository.findByFlashcardSet(game.getFlashcardSet());

            for (int i = 0; i < game.getNumberOfQuestions(); i++) {
                Flashcard fc = allFlashcards.get(random.nextInt(allFlashcards.size()));

                boolean askTerm = game.getQuestionType() == QuestionType.RANDOM ? random.nextBoolean()
                        : game.getQuestionType() == QuestionType.TERM;

                String questionText = askTerm ? fc.getTerm() : fc.getDefinition();
                String correctAnswer = askTerm ? fc.getDefinition() : fc.getTerm();

                // Prepare distractors list (exclude current flashcard)
                List<String> distractors = allFlashcards.stream()
                        .filter(f -> !f.getId().equals(fc.getId()))
                        .map(f -> askTerm ? f.getDefinition() : f.getTerm())
                        .distinct()
                        .collect(Collectors.toList());

                // Randomly pick up to 3 distractors
                Collections.shuffle(distractors);
                distractors = distractors.stream().limit(3).collect(Collectors.toList());

                // Add the correct answer and shuffle
                List<String> options = new ArrayList<>(distractors);
                options.add(correctAnswer);
                Collections.shuffle(options);

                questions.add(new QuestionDTO(fc.getId(), questionText, options, correctAnswer));
            }
        }
     else if (isQuizGame) {
            List<Question> quizQuestions = questionRepository.findByQuiz(game.getQuiz());

            for (int i = 0; i < game.getNumberOfQuestions(); i++) {
                Question qq = quizQuestions.get(random.nextInt(quizQuestions.size()));

                boolean askQuestionText = game.getQuestionType() == QuestionType.RANDOM ? random.nextBoolean()
                        : game.getQuestionType() == QuestionType.TERM;

                String questionText = askQuestionText ? qq.getQuestionText() : qq.getCorrectAnswer();
                String correctAnswer = askQuestionText ? qq.getCorrectAnswer() : qq.getQuestionText();

                // Get 3 distractors (mauvaise réponses), excluant la bonne
                List<String> distractors = questionRepository.findRandomAnswersExcluding(qq.getQuestionId(), 3);

                List<String> options = new ArrayList<>(distractors);
                options.add(correctAnswer);
                Collections.shuffle(options);

                questions.add(new QuestionDTO(qq.getQuestionId(), questionText, options, correctAnswer));
            }
        }

        return questions;
    }


    public String checkAnswer(Long gameId, Long questionId, String answer, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player= playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("player not found"));


        if (game.getFlashcardSet() != null) {
            Flashcard flashcard = flashcardRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));

            String correctAnswer = game.getQuestionType() == QuestionType.DEFINITION
                    ? flashcard.getTerm()
                    : flashcard.getDefinition();

            if (answer.equalsIgnoreCase(correctAnswer)) {
                game.getPlayerScores().put(playerId, game.getPlayerScores().getOrDefault(playerId, 0) + 1);
                player.setScore(player.getScore()+1);
                gameRepository.save(game);
                return "Correct!";
            } else {
                return "Incorrect. Try again.";
            }
        } else if (game.getQuiz() != null) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            String correctAnswer = question.getCorrectAnswer();

            if (answer.equalsIgnoreCase(correctAnswer)) {
                game.getPlayerScores().put(playerId, game.getPlayerScores().getOrDefault(playerId, 0) + 1);
                player.setScore(player.getScore()+1);
                gameRepository.save(game);
                return "Correct!";
            } else {
                return "Incorrect. Try again." + correctAnswer;
            }
        }

        throw new IllegalStateException("Game must have either flashcardSet or quiz");
    }

    private void pause4Seconds() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String finishGame(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player= playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("player not found"));
        player.setFinished(true);

        long finishTime = System.currentTimeMillis();
        game.getPlayerFinishTimes().put(playerId, finishTime);

        gameRepository.save(game);
        return "Player finished!";
    }

    public String endGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        game.setIsActive(false);
        gameRepository.save(game);

        List<Map.Entry<Long, Integer>> sortedPlayers = game.getPlayerScores().entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    int scoreCompare = e2.getValue().compareTo(e1.getValue());
                    if (scoreCompare != 0) return scoreCompare;
                    return game.getPlayerFinishTimes().get(e1.getKey())
                            .compareTo(game.getPlayerFinishTimes().get(e2.getKey()));
                })
                .toList();

        Long winnerId = sortedPlayers.get(0).getKey();
        Integer winnerScore = sortedPlayers.get(0).getValue();

        StringBuilder leaderboard = new StringBuilder("Leaderboard:\n");
        int rank = 1;
        for (Map.Entry<Long, Integer> entry : sortedPlayers) {
            leaderboard.append("Rank ").append(rank++)
                    .append(": Player ").append(entry.getKey())
                    .append(" - Score: ").append(entry.getValue()).append("\n");
        }
        leaderboard.append("Winner: Player ").append(winnerId)
                .append(" with ").append(winnerScore).append(" points!");

        return leaderboard.toString();
    }

    public List<Game> getActiveGames() {
        return gameRepository.findByIsActiveTrue();
    }

    public Game getGameByCode(String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with code " + gameCode));
    }

    public List<Player> getPlayersForGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return game.getPlayers();
    }

    public void updateScore(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Update the player’s score in the game
        game.getPlayerScores().put(playerId, game.getPlayerScores().getOrDefault(playerId, 0) + 1);
        player.setScore(player.getScore() + (long)1);

        // Save the updated game and player
        gameRepository.save(game);
        playerRepository.save(player);
    }
}
