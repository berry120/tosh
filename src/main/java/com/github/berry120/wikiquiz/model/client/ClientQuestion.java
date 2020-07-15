package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.Collection;

@Data
public class ClientQuestion implements ClientObject {

    private String type;
    private String question;
    private Collection<String> answers;

    public ClientQuestion(String question, Collection<String> answers) {
        this.type = "question";
        this.question = question;
        this.answers = answers;
    }

}
