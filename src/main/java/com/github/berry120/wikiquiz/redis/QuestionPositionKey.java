package com.github.berry120.wikiquiz.redis;

import lombok.Data;

@Data
public class QuestionPositionKey implements RedisKey {

    private String type;
    private String quizId;

    public QuestionPositionKey(String quizId) {
        this.type = "question_position";
        this.quizId = quizId;
    }
}
