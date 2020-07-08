package com.github.berry120.wikiquiz.model;

import com.github.berry120.wikiquiz.model.client.ClientAnswer;
import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.model.client.ClientQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    private String question;
    private String correctAnswer;
    private List<String> wrongAnswers;

    public ClientObject toClientQuestion() {
        List<String> answers = new ArrayList<>(wrongAnswers);
        answers.add(correctAnswer);
        Collections.sort(answers);
        return new ClientQuestion(question, answers);
    }

    public ClientObject toClientAnswer() {
        return new ClientAnswer(correctAnswer);
    }

}
