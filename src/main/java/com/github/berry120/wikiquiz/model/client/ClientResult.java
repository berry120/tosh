package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.Map;

@Data
public class ClientResult implements ClientObject {

    private String type;
    private Map<String, Integer> scores;

    public ClientResult(Map<String, Integer> scores) {
        type = "results";
        this.scores = scores;
    }

}
