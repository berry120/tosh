package com.github.berry120.wikiquiz.redis.model;

import lombok.Data;

@Data
public class QuizStateKey implements RedisKey {

    private String type;
    private String quizId;

    public QuizStateKey(String quizId) {
        this.type = "quizstate";
        this.quizId = quizId;
    }
}
