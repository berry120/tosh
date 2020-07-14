package com.github.berry120.wikiquiz.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.berry120.wikiquiz.model.Player;
import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.redis.model.AnswerKey;
import com.github.berry120.wikiquiz.redis.model.FakeAnswerKey;
import com.github.berry120.wikiquiz.redis.model.PlayerKey;
import com.github.berry120.wikiquiz.redis.model.PlayerScoreKey;
import com.github.berry120.wikiquiz.redis.model.QuestionNumberKey;
import com.github.berry120.wikiquiz.redis.model.QuizKey;
import com.github.berry120.wikiquiz.service.RandomIdGenerator;
import com.github.berry120.wikiquiz.service.ScoreCalculatorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RedisRepository {

    private final RedisOps redisOps;
    private final RandomIdGenerator idGenerator;
    private final ScoreCalculatorService scoreCalculatorService;

    @Inject
    public RedisRepository(RedisOps redisOps, RandomIdGenerator idGenerator, ScoreCalculatorService scoreCalculatorService) {
        this.redisOps = redisOps;
        this.idGenerator = idGenerator;
        this.scoreCalculatorService = scoreCalculatorService;
    }

    public void storeQuiz(Quiz quiz) {
        redisOps.set(new QuizKey(quiz.getId()), quiz);
    }

    public Quiz retrieveQuiz(String quizId) {
        return redisOps.get(new QuizKey(quizId), new TypeReference<Quiz>() {}).orElseThrow(() -> new RuntimeException("No quiz exists with id " + quizId));
    }

    public boolean quizExists(String quizId) {
        return redisOps.get(new QuizKey(quizId), new TypeReference<Quiz>() {}).isPresent();
    }

    public Map<String, Player> retrieveAllPlayers(String quizId) {
        return redisOps.get(new PlayerKey(quizId), new TypeReference<Map<String, Player>>() {}).orElse(new HashMap<>());
    }

    public boolean haveAllAnswers(String quizId) {
        return retrieveAnswers(quizId).size() >= retrieveAllPlayers(quizId).size();
    }

    public boolean haveAllFakeAnswers(String quizId) {
        return retrieveFakeAnswers(quizId).size() >= retrieveAllPlayers(quizId).size();
    }

    public void storePlayer(String quizId, Player player) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        players.put(player.getId(), player);
        redisOps.set(new PlayerKey(quizId), players);
    }

    public void removeTempQuestionData(String quizId) {
        for (String playerId : retrieveAllPlayers(quizId).keySet()) {
            redisOps.delete(new AnswerKey(quizId, playerId));
            redisOps.delete(new FakeAnswerKey(quizId, playerId));
        }
    }

    public void storeAnswer(String quizId, String playerId, String answer) {
        redisOps.set(new AnswerKey(quizId, playerId), answer);
    }

    public Map<Player, String> retrieveAnswers(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, String> ret = new HashMap<>();
        for (Player player : players.values()) {
            redisOps.get(new AnswerKey(quizId, player.getId()), new TypeReference<String>() {}).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }

    public void storeFakeAnswer(String quizId, String playerId, String fakeAnswer) {
        redisOps.set(new FakeAnswerKey(quizId, playerId), fakeAnswer);
    }

    public Map<Player, String> retrieveFakeAnswers(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, String> ret = new HashMap<>();
        for (Player player : players.values()) {
            redisOps.get(new FakeAnswerKey(quizId, player.getId()), new TypeReference<String>() {}).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }

    public String registerPlayer(String quizId, String playerName) {
        Player player = new Player(playerName, idGenerator.generateRandomId());
        storePlayer(quizId, player);
        return player.getId();
    }

    public void updateScores(String quizId) {
        scoreCalculatorService.getScores(
                retrieveQuiz(quizId).getQuestions().get(retrieveQuestionNumber(quizId)).getCorrectAnswer(),
                retrieveAnswers(quizId),
                retrieveFakeAnswers(quizId)
        ).forEach((player, score) -> addToScore(quizId, player.getId(), score));
    }

    public int retrieveQuestionNumber(String quizId) {
        return redisOps.get(new QuestionNumberKey(quizId), new TypeReference<Integer>() {}).orElse(-1);
    }

    public void storeQuestionNumber(String quizId, int questionNumber) {
        QuestionNumberKey key = new QuestionNumberKey(quizId);
        redisOps.set(key, questionNumber);
    }

    public void addToScore(String quizId, String playerId, int scoreToAdd) {
        PlayerScoreKey key = new PlayerScoreKey(quizId, playerId);
        int currentScore = redisOps.get(key, new TypeReference<Integer>() {}).orElse(0);
        redisOps.set(key, currentScore + scoreToAdd);
    }

    public Map<Player, Integer> retrieveScores(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, Integer> ret = new HashMap<>();
        for (Player player : players.values()) {
            redisOps.get(new PlayerScoreKey(quizId, player.getId()), new TypeReference<Integer>() {}).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }

}
