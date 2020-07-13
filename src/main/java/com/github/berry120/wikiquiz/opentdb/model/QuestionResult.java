package com.github.berry120.wikiquiz.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import java.util.List;
import java.util.stream.Collectors;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResult {

    @JsonProperty("response_code")
    private int responseCode;
    private List<Question> results;

    public QuestionResult decodeUrl() {
        return this
                .withResults(results.stream()
                        .map(Question::decodeUrl)
                        .collect(Collectors.toList()));
    }

}
