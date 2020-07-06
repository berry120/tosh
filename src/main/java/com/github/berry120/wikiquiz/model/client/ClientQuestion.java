package com.github.berry120.wikiquiz.model.client;

import lombok.Data;
import java.util.List;

@Data
public class ClientQuestion implements ClientObject {

    private String type;
    private String question;
    private List<String> answers;

    public ClientQuestion(String question, List<String> answers) {
        this.type = "question";
        this.question = question;
        this.answers = answers;
    }

}
