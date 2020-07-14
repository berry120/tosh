package com.github.berry120.wikiquiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizState {

    public static final QuizState INITIAL = new QuizState(-1, QuestionStage.SHOW_RESULTS);

    private int questionNumber;
    private QuestionStage questionStage;

}
