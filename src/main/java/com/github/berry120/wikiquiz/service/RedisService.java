package com.github.berry120.wikiquiz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.model.Player;
import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.redis.AnswerKey;
import com.github.berry120.wikiquiz.redis.FakeAnswerKey;
import com.github.berry120.wikiquiz.redis.PlayerKey;
import com.github.berry120.wikiquiz.redis.PlayerScoreKey;
import com.github.berry120.wikiquiz.redis.QuestionNumberKey;
import com.github.berry120.wikiquiz.redis.QuizKey;
import com.github.berry120.wikiquiz.redis.RedisKey;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class RedisService {

    private static final long EXPIRY_SECONDS = 60 * 60 * 6; //6 hours
    private final ObjectMapper mapper;
    private StatefulRedisConnection<String, String> connection;

    @ConfigProperty(name = "redis.host")
    String redisHost;
    @ConfigProperty(name = "redis.pwd")
    String redisPwd;
    @ConfigProperty(name = "redis.port")
    String redisPort;

    @Inject
    public RedisService() {
        mapper = new ObjectMapper();
    }

    public void connect() {
        if (connection == null || !connection.isOpen()) {
            RedisClient client = RedisClient.create("redis://" + redisPwd + "@" + redisHost + ":" + redisPort);
            connection = client.connect();
        }
    }

    public void storeQuiz(Quiz quiz) {
        System.out.println("STORING QUIZ " + quiz.getId());
        set(new QuizKey(quiz.getId()), quiz);
    }

    public Quiz retrieveQuiz(String quizId) {
        return get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).orElseThrow(() -> new RuntimeException("No quiz exists with id " + quizId));
    }

    public boolean quizExists(String quizId) {
        return get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).isPresent();
    }

    public Map<String, Player> retrieveAllPlayers(String quizId) {
        return get(new PlayerKey(quizId), new TypeReference<Map<String, Player>>() {
        }).orElse(new HashMap<>());
    }

    public void storePlayer(String quizId, Player player) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        players.put(player.getId(), player);
        set(new PlayerKey(quizId), players);
    }

    public void removeAnswers(String quizId) {
        for (String playerId : retrieveAllPlayers(quizId).keySet()) {
            delete(new AnswerKey(quizId, playerId));
        }
    }

    public void storeAnswer(String quizId, String playerId, String answer) {
        AnswerKey key = new AnswerKey(quizId, playerId);
        set(key, answer);
    }

    public Map<Player, String> retrieveAnswers(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, String> ret = new HashMap<>();
        for (Player player : players.values()) {
            get(new AnswerKey(quizId, player.getId()), new TypeReference<String>() {
            }).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }

    public void removeFakeAnswers(String quizId) {
        for (String playerId : retrieveAllPlayers(quizId).keySet()) {
            delete(new FakeAnswerKey(quizId, playerId));
        }
    }

    public void storeFakeAnswer(String quizId, String playerId, String fakeAnswer) {
        FakeAnswerKey key = new FakeAnswerKey(quizId, playerId);
        set(key, fakeAnswer);
    }

    public Map<Player, String> retrieveFakeAnswers(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, String> ret = new HashMap<>();
        for (Player player : players.values()) {
            get(new FakeAnswerKey(quizId, player.getId()), new TypeReference<String>() {
            }).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }

    public int retrieveQuestionNumber(String quizId) {
        return get(new QuestionNumberKey(quizId), new TypeReference<Integer>() {
        }).orElse(-1);
    }

    public void storeQuestionNumber(String quizId, int questionNumber) {
        QuestionNumberKey key = new QuestionNumberKey(quizId);
        set(key, questionNumber);
    }

    public void addToScore(String quizId, String playerId, int scoreToAdd) {
        PlayerScoreKey key = new PlayerScoreKey(quizId, playerId);
        int currentScore = get(key, new TypeReference<Integer>() {
        }).orElse(0);
        set(key, currentScore + scoreToAdd);
    }

    public Map<Player, Integer> retrieveScores(String quizId) {
        Map<String, Player> players = retrieveAllPlayers(quizId);
        Map<Player, Integer> ret = new HashMap<>();
        for (Player player : players.values()) {
            get(new PlayerScoreKey(quizId, player.getId()), new TypeReference<Integer>() {
            }).ifPresent(a -> ret.put(player, a));
        }
        return ret;
    }


    private <T> Optional<T> get(RedisKey redisKey, TypeReference<T> type) {
        connect();
        try {
            String json = connection.sync().get(mapper.writeValueAsString(redisKey));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void set(RedisKey redisKey, Object obj) {
        connect();
        try {
            SetArgs args = SetArgs.Builder.ex(EXPIRY_SECONDS);
            String key = mapper.writeValueAsString(redisKey);
            String value = mapper.writeValueAsString(obj);
            connection.sync().set(key, value, args);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void delete(RedisKey redisKey) {
        connect();
        try {
            String key = mapper.writeValueAsString(redisKey);
            connection.sync().del(key);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
