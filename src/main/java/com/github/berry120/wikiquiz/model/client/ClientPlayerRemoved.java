package com.github.berry120.wikiquiz.model.client;

import lombok.Data;

@Data
public class ClientPlayerRemoved implements ClientObject {

    private String type;
    private String playerName;

    public ClientPlayerRemoved(String playerName) {
        this.type = "player_removed";
        this.playerName = playerName;
    }
}
