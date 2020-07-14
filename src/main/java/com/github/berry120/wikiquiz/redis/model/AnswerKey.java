package com.github.berry120.wikiquiz.redis.model;

import lombok.Data;

@Data
public class AnswerKey implements RedisKey {

    private String type;
    private String quizId;
    private String playerId;

    public AnswerKey(String quizId, String playerId) {
        this.type = "answer";
        this.quizId = quizId;
        this.playerId = playerId;
    }
}
