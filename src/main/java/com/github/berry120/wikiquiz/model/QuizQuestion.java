package com.github.berry120.wikiquiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    private String question;
    private String correctAnswer;
    private List<String> sampleWrongAnswers;

}
