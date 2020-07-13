package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Player;
import com.github.berry120.wikiquiz.model.Quiz;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class QuizStateService {

    private final RandomIdGenerator idGenerator;
    private final RedisService redisService;
    private final ScoreCalculatorService scoreCalculatorService;

    @Inject
    QuizStateService(RandomIdGenerator idGenerator, RedisService redisService, ScoreCalculatorService scoreCalculatorService) {
        this.idGenerator = idGenerator;
        this.redisService = redisService;
        this.scoreCalculatorService = scoreCalculatorService;
    }

    public Quiz getQuiz(String quizId) {
        return redisService.retrieveQuiz(quizId);
    }

    public boolean quizExists(String quizId) {
        return redisService.quizExists(quizId);
    }

    public void registerQuiz(Quiz quiz) {
        redisService.storeQuiz(quiz);
    }

    public void removeExistingAnswers(String quizId) {
        redisService.removeAnswers(quizId);
        redisService.removeFakeAnswers(quizId);
    }

    public void storeFakeAnswer(String quizId, String playerId, String fakeAnswer) {
        redisService.storeFakeAnswer(quizId, playerId, fakeAnswer);
    }

    public boolean haveAllFakeAnswers(String quizId) {
        return redisService.retrieveFakeAnswers(quizId).size() >= redisService.retrieveAllPlayers(quizId).size();
    }

    public Map<Player, String> getFakeAnswers(String quizId) {
        return redisService.retrieveFakeAnswers(quizId);
    }

    public void storeAnswer(String quizId, String playerId, String answer) {
        redisService.storeAnswer(quizId, playerId, answer);
    }

    public boolean haveAllAnswers(String quizId) {
        System.out.println("HAVE ALL ANSWERS: " + redisService.retrieveAnswers(quizId).size());
        System.out.println("HAVE ALL ANSWERS: " + redisService.retrieveAllPlayers(quizId).size());
        return redisService.retrieveAnswers(quizId).size() >= redisService.retrieveAllPlayers(quizId).size();
    }

    public Map<Player, String> getAnswers(String quizId) {
        return redisService.retrieveAnswers(quizId);
    }

    public String addPlayer(String quizId, String playerName) {
        Player player = new Player(playerName, idGenerator.generateRandomId());
        redisService.storePlayer(quizId, player);
        return player.getId();
    }

    public int getQuestionNumber(String quizId) {
        return redisService.retrieveQuestionNumber(quizId);
    }

    public void setQuestionNumber(String quizId, int questionNumber) {
        redisService.storeQuestionNumber(quizId, questionNumber);
    }

    public void updateScores(String quizId) {
        String correctAnswer = getQuiz(quizId).getQuestions().get(redisService.retrieveQuestionNumber(quizId)).getCorrectAnswer();
        Map<Player, String> answers = redisService.retrieveAnswers(quizId);
        Map<Player, String> fakeAnswers = redisService.retrieveFakeAnswers(quizId);

        scoreCalculatorService.getScores(correctAnswer, answers, fakeAnswers)
                .forEach((player, score) -> redisService.addToScore(quizId, player.getId(), score));
    }

    public Map<Player, Integer> getScores(String quizId) {
        return redisService.retrieveScores(quizId);
    }

}
