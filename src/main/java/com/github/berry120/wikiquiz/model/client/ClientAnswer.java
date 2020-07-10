package com.github.berry120.wikiquiz.model.client;

import com.github.berry120.wikiquiz.model.Player;
import lombok.Data;
import java.util.Map;

@Data
public class ClientAnswer implements ClientObject {

    private String type;
    private String correctAnswer;
    private Map<Player, String> answers;
    private Map<Player, String> fakeAnswers;
    private Map<Player, Integer> scores;

    public ClientAnswer(String correctAnswer, Map<Player, String> answers, Map<Player, String> fakeAnswers, Map<Player, Integer> scores) {
        this.type = "answer";
        this.correctAnswer = correctAnswer;
        this.answers = answers;
        this.fakeAnswers = fakeAnswers;
        this.scores = scores;
    }
}
