package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.Set;

@Data
public class ClientQuestion implements ClientObject {

    private String type;
    private String question;
    private Set<String> answers;

    public ClientQuestion(String question, Set<String> answers) {
        this.type = "question";
        this.question = question;
        this.answers = answers;
    }

}
