package com.github.berry120.wikiquiz.model.client;

import lombok.Data;

@Data
public class ClientFakeAnswerRequest implements ClientObject {

    private String type;
    private String question;
    private int questionNumber;

    public ClientFakeAnswerRequest(String question, int questionNumber) {
        this.type = "fake_answer_request";
        this.question = question;
        this.questionNumber = questionNumber;
    }

}