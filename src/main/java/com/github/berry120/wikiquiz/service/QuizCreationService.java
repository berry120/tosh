package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.opentdb.OpenTdbService;
import com.github.berry120.wikiquiz.redis.RedisRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizCreationService {

    private final RandomIdGenerator idGenerator;
    private final OpenTdbService openTdbService;
    private final RedisRepository redisRepository;

    @Inject
    QuizCreationService(OpenTdbService openTdbService, RandomIdGenerator idGenerator, RedisRepository redisRepository) {
        this.idGenerator = idGenerator;
        this.openTdbService = openTdbService;
        this.redisRepository = redisRepository;
    }

    public Quiz createQuiz() {
        Quiz quiz = new Quiz(
                idGenerator.generateRandomId(),
                openTdbService.retrieveAppropriateQuestionSet()
                        .stream()
                        .map(q -> new QuizQuestion(q.getQuestion(), q.getCorrectAnswer(), q.getIncorrectAnswers()))
                        .collect(Collectors.toList())
        );
        redisRepository.storeQuiz(quiz);
        return quiz;
    }
}
