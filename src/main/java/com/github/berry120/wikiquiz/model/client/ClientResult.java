package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.List;

@Data
public class ClientResult implements ClientObject {

    private String type;
    private List<PersonScore> scores;

    public ClientResult(List<PersonScore> scores) {
        type = "results";
        this.scores = scores;
    }

}
