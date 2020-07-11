package com.github.berry120.wikiquiz.model.client;

import lombok.Data;

@Data
public class ClientPlayerJoined implements ClientObject {

    private String type;
    private String playerName;

    public ClientPlayerJoined(String playerName) {
        this.type = "player_joined";
        this.playerName = playerName;
    }
}
