package com.github.berry120.wikiquiz.opentdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.opentdb.model.Difficulty;
import com.github.berry120.wikiquiz.opentdb.model.Encoding;
import com.github.berry120.wikiquiz.opentdb.model.Question;
import com.github.berry120.wikiquiz.opentdb.model.QuestionResult;
import com.github.berry120.wikiquiz.opentdb.model.Type;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
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
                        .encoding(Encoding.URL)
                        .build())
                .stream()
                .filter(q -> !q.getQuestion().contains("of these"))
                .filter(q -> !q.getQuestion().contains("not say"))
                .filter(q -> !q.getQuestion().contains("is not"))
                .limit(5)
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
                                    .decodeUrl().getResults();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }


}
