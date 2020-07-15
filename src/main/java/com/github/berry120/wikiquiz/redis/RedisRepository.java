package com.github.berry120.wikiquiz.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizState;
import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import com.github.berry120.wikiquiz.redis.model.AnswerKey;
import com.github.berry120.wikiquiz.redis.model.FakeAnswerKey;
import com.github.berry120.wikiquiz.redis.model.PlayerKey;
import com.github.berry120.wikiquiz.redis.model.PlayerScoreKey;
import com.github.berry120.wikiquiz.redis.model.QuizKey;
import com.github.berry120.wikiquiz.redis.model.QuizStateKey;
import com.github.berry120.wikiquiz.service.ScoreCalculatorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class RedisRepository {

    private final RedisOps redisOps;
    private final ScoreCalculatorService scoreCalculatorService;

    @Inject
    public RedisRepository(RedisOps redisOps, ScoreCalculatorService scoreCalculatorService) {
        this.redisOps = redisOps;
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

    public Set<PlayerDetails> retrievePlayers(String quizId) {
        return redisOps.get(new PlayerKey(quizId), new TypeReference<Set<PlayerDetails>>() {}).orElse(new HashSet<>());
    }

    public boolean haveAllAnswers(String quizId) {
        return retrieveAnswers(quizId).size() >= retrievePlayers(quizId).size();
    }

    public boolean haveAllFakeAnswers(String quizId) {
        return retrieveFakeAnswers(quizId).size() >= retrievePlayers(quizId).size();
    }

    public boolean storePlayer(String quizId, PlayerDetails playerDetails) {
        Set<PlayerDetails> players = retrievePlayers(quizId);
        for (PlayerDetails player : players) {
            if (player.getName().equals(playerDetails.getName())) {
                return false;
            }
        }

        boolean storedOk = players.add(playerDetails);
        if (storedOk) {
            redisOps.set(new PlayerKey(quizId), players);
        }
        return storedOk;
    }

    public void removePlayer(String quizId, PlayerDetails playerDetails) {
        Set<PlayerDetails> players = retrievePlayers(quizId);
        players.remove(playerDetails);
        redisOps.set(new PlayerKey(quizId), players);
    }

    public void removeTempQuestionData(String quizId) {
        for (PlayerDetails playerDetails : retrievePlayers(quizId)) {
            redisOps.delete(new AnswerKey(quizId, playerDetails));
            redisOps.delete(new FakeAnswerKey(quizId, playerDetails));
        }
    }

    public void storeAnswer(String quizId, PlayerDetails playerDetails, String answer) {
        redisOps.set(new AnswerKey(quizId, playerDetails), answer);
    }

    public Map<PlayerDetails, String> retrieveAnswers(String quizId) {
        Map<PlayerDetails, String> ret = new HashMap<>();
        for (PlayerDetails playerDetails : retrievePlayers(quizId)) {
            redisOps.get(new AnswerKey(quizId, playerDetails), new TypeReference<String>() {}).ifPresent(a -> ret.put(playerDetails, a));
        }
        return ret;
    }

    public void storeFakeAnswer(String quizId, PlayerDetails playerDetails, String fakeAnswer) {
        redisOps.set(new FakeAnswerKey(quizId, playerDetails), fakeAnswer);
    }

    public Map<PlayerDetails, String> retrieveFakeAnswers(String quizId) {
        Set<PlayerDetails> players = retrievePlayers(quizId);
        Map<PlayerDetails, String> ret = new HashMap<>();
        for (PlayerDetails playerDetails : players) {
            redisOps.get(new FakeAnswerKey(quizId, playerDetails), new TypeReference<String>() {}).ifPresent(a -> ret.put(playerDetails, a));
        }
        return ret;
    }

    public void updateScores(String quizId) {
        scoreCalculatorService.getScores(
                retrieveQuiz(quizId).getQuestions().get(retrieveQuizState(quizId).getQuestionNumber()).getCorrectAnswer(),
                retrieveAnswers(quizId),
                retrieveFakeAnswers(quizId)
        ).forEach((player, score) -> addToScore(quizId, player, score));
    }

    public QuizState retrieveQuizState(String quizId) {
        return redisOps.get(new QuizStateKey(quizId), new TypeReference<QuizState>() {}).orElse(QuizState.INITIAL);
    }

    public void storeQuizState(String quizId, QuizState quizState) {
        System.out.println("Changing state to: " + quizState);
        QuizStateKey key = new QuizStateKey(quizId);
        redisOps.set(key, quizState);
    }

    public void addToScore(String quizId, PlayerDetails playerDetails, int scoreToAdd) {
        PlayerScoreKey key = new PlayerScoreKey(quizId, playerDetails);
        int currentScore = redisOps.get(key, new TypeReference<Integer>() {}).orElse(0);
        redisOps.set(key, currentScore + scoreToAdd);
    }

    public Map<String, Integer> retrieveScores(String quizId) {
        Set<PlayerDetails> players = retrievePlayers(quizId);
        Map<String, Integer> scores = new HashMap<>();
        for (PlayerDetails player : players) {
            redisOps.get(new PlayerScoreKey(quizId, player), new TypeReference<Integer>() {}).ifPresent(a -> scores.put(player.getName(), a));
        }
        return scores;
    }

}
