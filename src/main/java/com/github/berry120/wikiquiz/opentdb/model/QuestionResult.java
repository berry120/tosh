package com.github.berry120.wikiquiz.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class QuestionResult {

    @JsonProperty("response_code")
    private int responseCode;
    private List<Question> results;

}
