package com.github.berry120.wikiquiz.model.client;

import lombok.Data;

@Data
public class ClientFakeAnswerRequest implements ClientObject {

    private String type;
    private String question;
    private String answerHash;
    private int questionNumber;

    public ClientFakeAnswerRequest(String question, String answerHash, int questionNumber) {
        this.type = "fake_answer_request";
        this.question = question;
        this.answerHash = answerHash;
        this.questionNumber = questionNumber;
    }

}