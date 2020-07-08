package com.github.berry120.wikiquiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkedAnswer {

    private QuizAnswer answer;
    private boolean correct;
}
