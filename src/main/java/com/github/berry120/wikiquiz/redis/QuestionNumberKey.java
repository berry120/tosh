package com.github.berry120.wikiquiz.redis;

import lombok.Data;

@Data
public class QuestionNumberKey implements RedisKey {

    private String type;
    private String quizId;

    public QuestionNumberKey(String quizId) {
        this.type = "questionnumber";
        this.quizId = quizId;
    }
}
