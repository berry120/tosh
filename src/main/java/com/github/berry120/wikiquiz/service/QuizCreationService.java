package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.opentdb.OpenTdbService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizCreationService {

    private final RandomIdGenerator idGenerator;
    private final OpenTdbService openTdbService;

    @Inject
    QuizCreationService(OpenTdbService openTdbService, RandomIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.openTdbService = openTdbService;
    }

    public Quiz createQuiz() {
        return new Quiz(
                idGenerator.generateRandomId(),
                openTdbService.retrieveAppropriateQuestionSet()
                        .stream()
                        .map(q -> new QuizQuestion(q.getQuestion(), q.getCorrectAnswer(), q.getIncorrectAnswers()))
                        .collect(Collectors.toList())
        );
    }
}
