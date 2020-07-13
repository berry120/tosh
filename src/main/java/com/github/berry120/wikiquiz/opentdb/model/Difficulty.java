package com.github.berry120.wikiquiz.opentdb.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Difficulty {

    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String difficulty;

    public String toParamValue() {
        return difficulty;
    }
}
