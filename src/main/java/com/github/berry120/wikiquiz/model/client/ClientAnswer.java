package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ClientAnswer implements ClientObject {

    private String type;
    private int questionIdx;
    private String correctAnswer;
    private Map<String, List<String>> answers;
    private Map<String, List<String>> fakeAnswers;
    private Map<String, Integer> scores;

    public ClientAnswer(String correctAnswer, int questionIdx, Map<String, List<String>> answers, Map<String, List<String>> fakeAnswers, Map<String, Integer> scores) {
        this.type = "answer";
        this.questionIdx = questionIdx;
        this.correctAnswer = correctAnswer;
        this.answers = answers;
        this.fakeAnswers = fakeAnswers;
        this.scores = scores;
    }
}
