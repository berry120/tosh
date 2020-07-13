package com.github.berry120.wikiquiz.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Data
@With
@AllArgsConstructor
public class Question {

    private String category;
    private String type;
    private String difficulty;
    private String question;
    @JsonProperty("correct_answer")
    private String correctAnswer;
    @JsonProperty("incorrect_answers")
    private List<String> incorrectAnswers;

    public Question decodeUrl() {
        try {
            return this
                    .withCategory(URLDecoder.decode(category, StandardCharsets.UTF_8.name()))
                    .withType(URLDecoder.decode(type, StandardCharsets.UTF_8.name()))
                    .withDifficulty(URLDecoder.decode(difficulty, StandardCharsets.UTF_8.name()))
                    .withQuestion(URLDecoder.decode(question, StandardCharsets.UTF_8.name()))
                    .withCorrectAnswer(URLDecoder.decode(correctAnswer, StandardCharsets.UTF_8.name()))
                    .withIncorrectAnswers(incorrectAnswers
                            .stream()
                            .map(s -> {
                                try {
                                    return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

}
