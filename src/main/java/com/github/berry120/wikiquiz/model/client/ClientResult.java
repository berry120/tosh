package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.List;

@Data
public class ClientResult implements ClientObject {

    private String type;
    private List<ClientScore> scores;

    public ClientResult(List<ClientScore> scores) {
        type = "results";
        this.scores = scores;
    }

}
