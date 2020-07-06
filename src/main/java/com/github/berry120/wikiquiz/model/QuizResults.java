package com.github.berry120.wikiquiz.model;

import com.github.berry120.wikiquiz.model.client.ClientResult;
import com.github.berry120.wikiquiz.model.client.PersonScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuizResults {

    private List<PersonScore> scores;

    public ClientResult toClientResults() {
        return new ClientResult(scores);
    }
}
