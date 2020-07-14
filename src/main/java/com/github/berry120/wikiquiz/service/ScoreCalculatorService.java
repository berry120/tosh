package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ScoreCalculatorService {

    public static final int POINTS_FOR_CORRECT_ANSWER = 100;
    public static final int POINTS_FOR_FOOLING_PLAYER = 40;

    public Map<PlayerDetails, Integer> getScores(String correctAnswer, Map<PlayerDetails, String> answers, Map<PlayerDetails, String> fakeAnswers) {
        Set<PlayerDetails> players = new HashSet<>(answers.keySet());
        players.addAll(fakeAnswers.keySet());

        Map<PlayerDetails, Integer> scores = new HashMap<>();
        for (PlayerDetails player : players) {
            int score = 0;
            if (correctAnswer.equals(answers.get(player))) {
                score += POINTS_FOR_CORRECT_ANSWER;
            }
            String playersFakeAnswer = fakeAnswers.get(player);
            for (Map.Entry<PlayerDetails, String> answer : answers.entrySet()) {
                if (!answer.getKey().equals(player) && answer.getValue().equals(playersFakeAnswer)) {
                    score += POINTS_FOR_FOOLING_PLAYER;
                }
            }
            scores.put(player, score);
        }
        return scores;
    }
}
