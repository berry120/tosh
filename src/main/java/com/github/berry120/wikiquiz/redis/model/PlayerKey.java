package com.github.berry120.wikiquiz.redis.model;

import lombok.Data;

@Data
public class PlayerKey implements RedisKey {

    private String type;
    private String quizId;

    public PlayerKey(String quizId) {
        this.type = "player";
        this.quizId = quizId;
    }
}
