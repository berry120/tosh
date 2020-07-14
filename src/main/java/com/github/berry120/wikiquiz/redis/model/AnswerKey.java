package com.github.berry120.wikiquiz.redis.model;

import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import lombok.Data;

@Data
public class AnswerKey implements RedisKey {

    private String type;
    private String quizId;
    private PlayerDetails playerDetails;

    public AnswerKey(String quizId, PlayerDetails playerDetails) {
        this.type = "answer";
        this.quizId = quizId;
        this.playerDetails = playerDetails;
    }
}
