package com.github.berry120.wikiquiz.redis.model;

import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import lombok.Data;

@Data
public class FakeAnswerKey implements RedisKey {

    private String type;
    private String quizId;
    private PlayerDetails playerDetails;

    public FakeAnswerKey(String quizId, PlayerDetails playerDetails) {
        this.type = "fakeanswer";
        this.quizId = quizId;
        this.playerDetails = playerDetails;
    }
}
