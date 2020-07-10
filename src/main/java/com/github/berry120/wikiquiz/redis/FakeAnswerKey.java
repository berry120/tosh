package com.github.berry120.wikiquiz.redis;

import lombok.Data;

@Data
public class FakeAnswerKey implements RedisKey {

    private String type;
    private String quizId;
    private String playerId;

    public FakeAnswerKey(String quizId, String playerId) {
        this.type = "fakeanswer";
        this.quizId = quizId;
        this.playerId = playerId;
    }
}
