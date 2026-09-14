package com.lingobingo.Service;

import com.lingobingo.Dto.QuestionDTO;
import com.lingobingo.Dto.GameStateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.ConcurrentHashMap;
import com.lingobingo.Entity.*;
import com.lingobingo.Repository.*;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, Object> gameLocks = new ConcurrentHashMap<>();

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

    /** Start a synchronized game: freeze one shared question set and go ACTIVE. */
    public boolean startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getPlayers() == null || game.getPlayers().isEmpty()) {
            return false; // need at least one player
        }
        List<QuestionDTO> questions = generateSharedQuestions(game);
        if (questions.isEmpty()) {
            return false;
        }
        try {
            game.setQuestionsJson(objectMapper.writeValueAsString(questions));
        } catch (Exception e) {
            return false;
        }
        game.setPhase("ACTIVE");
        game.setIsActive(true);
        game.setCurrentQuestionIndex(0);
        game.setCurrentAnsweredBy(null);
        game.setLastAnsweredIndex(-1);
        game.setLastWinnerName(null);
        if (game.getPlayerScores() == null) game.setPlayerScores(new HashMap<>());
        for (Player p : game.getPlayers()) game.getPlayerScores().putIfAbsent(p.getId(), 0);
        gameRepository.save(game);
        return true;
    }

    // ---------- Synchronized (Kahoot-style) live game ----------

    /** A fixed, distinct, shuffled question set generated once when the game starts. */
    private List<QuestionDTO> generateSharedQuestions(Game game) {
        List<QuestionDTO> out = new ArrayList<>();
        Random random = new Random();

        if (game.getQuiz() != null) {
            List<Question> qs = new ArrayList<>(questionRepository.findByQuiz(game.getQuiz()));
            Collections.shuffle(qs);
            int n = game.getNumberOfQuestions() > 0 ? Math.min(game.getNumberOfQuestions(), qs.size()) : qs.size();
            for (int i = 0; i < n; i++) {
                Question qq = qs.get(i);
                String correct = (qq.getAnswers() != null && qq.getCorrectAnswer() != null)
                        ? qq.getAnswers().getOrDefault(qq.getCorrectAnswer(), qq.getCorrectAnswer())
                        : qq.getCorrectAnswer();
                List<String> options = new ArrayList<>();
                if (qq.getAnswers() != null && !qq.getAnswers().isEmpty()) options.addAll(qq.getAnswers().values());
                else if (correct != null) options.add(correct);
                Collections.shuffle(options);
                out.add(new QuestionDTO(qq.getQuestionId(), qq.getQuestionText(), options, correct));
            }
        } else if (game.getFlashcardSet() != null) {
            List<Flashcard> cards = new ArrayList<>(flashcardRepository.findByFlashcardSet(game.getFlashcardSet()));
            Collections.shuffle(cards);
            int n = game.getNumberOfQuestions() > 0 ? Math.min(game.getNumberOfQuestions(), cards.size()) : cards.size();
            for (int i = 0; i < n; i++) {
                Flashcard fc = cards.get(i);
                boolean askTerm = game.getQuestionType() == QuestionType.RANDOM ? random.nextBoolean()
                        : game.getQuestionType() != QuestionType.DEFINITION;
                String qtext = askTerm ? fc.getTerm() : fc.getDefinition();
                String correct = askTerm ? fc.getDefinition() : fc.getTerm();
                List<String> distractors = cards.stream()
                        .filter(f -> !f.getId().equals(fc.getId()))
                        .map(f -> askTerm ? f.getDefinition() : f.getTerm())
                        .distinct().collect(Collectors.toList());
                Collections.shuffle(distractors);
                List<String> options = new ArrayList<>(distractors.stream().limit(3).collect(Collectors.toList()));
                options.add(correct);
                Collections.shuffle(options);
                out.add(new QuestionDTO(fc.getId(), qtext, options, correct));
            }
        }
        return out;
    }

    private List<QuestionDTO> readQuestions(Game game) {
        if (game.getQuestionsJson() == null || game.getQuestionsJson().isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(game.getQuestionsJson(), new TypeReference<List<QuestionDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** Live state polled by every client. Never includes correct answers. */
    public GameStateDto getState(String gameCode) {
        Game game = getGameByCode(gameCode);
        List<QuestionDTO> qs = readQuestions(game);
        int total = qs.size();

        GameStateDto.StateQuestion sq = null;
        if ("ACTIVE".equals(game.getPhase()) && game.getCurrentQuestionIndex() < total) {
            QuestionDTO q = qs.get(game.getCurrentQuestionIndex());
            sq = new GameStateDto.StateQuestion(game.getCurrentQuestionIndex(), q.getQuestionText(), q.getOptions());
        }

        List<GameStateDto.PlayerScore> players = new ArrayList<>();
        Map<Long, Integer> scores = game.getPlayerScores() != null ? game.getPlayerScores() : new HashMap<>();
        if (game.getPlayers() != null) {
            for (Player p : game.getPlayers())
                players.add(new GameStateDto.PlayerScore(p.getId(), p.getUsername(), scores.getOrDefault(p.getId(), 0)));
        }
        players.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        return new GameStateDto(gameCode, game.getPhase(), game.getCurrentQuestionIndex(), total, sq, players,
                game.getCurrentAnsweredBy() != null, game.getLastWinnerName(), game.getLastAnsweredIndex());
    }

    /** A player answers the current question. First correct answer scores and advances everyone. */
    public Map<String, Object> submitAnswer(Long gameId, Long playerId, int questionIndex, String answer) {
        Map<String, Object> res = new HashMap<>();
        synchronized (lockFor(gameId)) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));
            if (!"ACTIVE".equals(game.getPhase()) || questionIndex != game.getCurrentQuestionIndex()) {
                res.put("correct", false); res.put("status", "tooLate"); return res;
            }
            List<QuestionDTO> qs = readQuestions(game);
            if (questionIndex < 0 || questionIndex >= qs.size()) {
                res.put("correct", false); res.put("status", "invalid"); return res;
            }
            String correct = qs.get(questionIndex).getCorrectAnswer();
            boolean isCorrect = answer != null && correct != null && answer.trim().equalsIgnoreCase(correct.trim());
            if (!isCorrect) { res.put("correct", false); res.put("status", "wrong"); return res; }
            if (game.getCurrentAnsweredBy() != null) { res.put("correct", false); res.put("status", "tooLate"); return res; }

            // First correct answer wins the question.
            game.setCurrentAnsweredBy(playerId);
            if (game.getPlayerScores() == null) game.setPlayerScores(new HashMap<>());
            game.getPlayerScores().put(playerId, game.getPlayerScores().getOrDefault(playerId, 0) + 100);
            Player winner = playerRepository.findById(playerId).orElse(null);
            if (winner != null) {
                winner.setScore((winner.getScore() == null ? 0L : winner.getScore()) + 100);
                playerRepository.save(winner);
                game.setLastWinnerName(winner.getUsername());
            }
            game.setLastAnsweredIndex(questionIndex);
            advance(game, qs.size());
            gameRepository.save(game);
            res.put("correct", true); res.put("status", "correct"); return res;
        }
    }

    /** Host forces the game to the next question (e.g. nobody got it). */
    public void nextQuestion(Long gameId) {
        synchronized (lockFor(gameId)) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));
            if (!"ACTIVE".equals(game.getPhase())) return;
            List<QuestionDTO> qs = readQuestions(game);
            if (game.getCurrentAnsweredBy() == null) {
                game.setLastWinnerName(null);
                game.setLastAnsweredIndex(game.getCurrentQuestionIndex());
            }
            advance(game, qs.size());
            gameRepository.save(game);
        }
    }

    private void advance(Game game, int total) {
        int next = game.getCurrentQuestionIndex() + 1;
        game.setCurrentAnsweredBy(null);
        if (next >= total) {
            game.setPhase("ENDED");
            game.setIsActive(false);
            game.setCurrentQuestionIndex(total);
        } else {
            game.setCurrentQuestionIndex(next);
        }
    }

    private Object lockFor(Long gameId) {
        return gameLocks.computeIfAbsent(gameId, k -> new Object());
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
