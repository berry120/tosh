package com.github.berry120.wikiquiz.redis.model;

import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import lombok.Data;

@Data
public class PlayerScoreKey implements RedisKey {

    private String type;
    private String quizId;
    private PlayerDetails playerDetails;

    public PlayerScoreKey(String quizId, PlayerDetails playerDetails) {
        this.type = "playerscore";
        this.quizId = quizId;
        this.playerDetails = playerDetails;
    }
}
