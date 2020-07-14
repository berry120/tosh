package com.github.berry120.wikiquiz.redis.model;

import lombok.Data;

@Data
public class QuizKey implements RedisKey {

    private String type;
    private String quizId;

    public QuizKey(String quizId) {
        this.type = "quiz";
        this.quizId = quizId;
    }
}
