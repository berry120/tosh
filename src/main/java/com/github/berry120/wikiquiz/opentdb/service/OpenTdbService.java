package com.github.berry120.wikiquiz.opentdb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.opentdb.OpenTdbRequest;
import com.github.berry120.wikiquiz.opentdb.model.Difficulty;
import com.github.berry120.wikiquiz.opentdb.model.Question;
import com.github.berry120.wikiquiz.opentdb.model.QuestionResult;
import com.github.berry120.wikiquiz.opentdb.model.Type;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class OpenTdbService {

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    @Inject
    OpenTdbService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        client = HttpClient.newHttpClient();
    }

    public List<Question> retrieveAppropriateQuestionSet() {
        return retrieveQuestions(
                OpenTdbRequest.builder()
                        .difficulty(Difficulty.HARD)
                        .numQuestions(100)
                        .type(Type.MULTIPLE_CHOICE)
                        .build())
                .stream()
                .filter(q -> !q.getQuestion().contains("of these"))
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<Question> retrieveQuestions(OpenTdbRequest tdbRequest) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tdbRequest.getRequestUrl()))
                .GET()
                .build();

        try {
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(r -> {
                        try {
                            return objectMapper.readValue(r.body(), QuestionResult.class)
                                    .getResults();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }


}
