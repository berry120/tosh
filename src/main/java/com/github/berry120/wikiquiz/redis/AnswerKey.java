package com.github.berry120.wikiquiz.redis;

import lombok.Data;

@Data
public class AnswerKey implements RedisKey {

    private String type;
    private String quizId;
    private int questionPosition;

    public AnswerKey(String quizId, int questionPosition) {
        this.type = "answer";
        this.quizId = quizId;
        this.questionPosition = questionPosition;
    }
}
