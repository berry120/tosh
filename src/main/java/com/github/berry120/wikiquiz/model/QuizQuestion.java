package com.github.berry120.wikiquiz.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
public class QuizQuestion {

    private String question;
    private String correctAnswer;

    public QuizQuestion(String question, String correctAnswer) {
        this.question = question;
        this.correctAnswer = correctAnswer;
    }

}
