package com.github.berry120.wikiquiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAnswer {

    private String personId;
    private String answer;

}
