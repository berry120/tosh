package com.github.berry120.wikiquiz.model;

public enum QuestionStage {

    FAKE_ANSWER_SUBMISSION,
    PICK_ANSWER,
    SHOW_RESULTS;

    public QuestionStage rewind() {
        if (this == SHOW_RESULTS) return PICK_ANSWER;
        if (this == PICK_ANSWER) return FAKE_ANSWER_SUBMISSION;
        if (this == FAKE_ANSWER_SUBMISSION) return SHOW_RESULTS;
        throw new AssertionError("Unhandled case");
    }
}
