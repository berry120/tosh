package com.github.berry120.wikiquiz.model.client;

import lombok.Data;

@Data
public class ClientAnswer implements ClientObject {

    private String type;
    private String answer;

    public ClientAnswer(String answer) {
        this.type = "answer";
        this.answer = answer;
    }
}
