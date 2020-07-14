package com.github.berry120.wikiquiz.redis.model;

import lombok.Data;

@Data
public class PlayerScoreKey implements RedisKey {

    private String type;
    private String quizId;
    private String playerId;

    public PlayerScoreKey(String quizId, String playerId) {
        this.type = "playerscore";
        this.quizId = quizId;
        this.playerId = playerId;
    }
}
