package com.github.berry120.wikiquiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    private String id;
    private List<QuizQuestion> questions;

    public List<QuizQuestion> getQuestions() {
        return new ArrayList<>(questions);
    }

}
