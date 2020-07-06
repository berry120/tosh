package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ApplicationScoped
public class QuizEventScheduler {

    private final ScheduledExecutorService executorService;

    @Inject
    QuizEventScheduler() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startQuiz(Quiz quiz) {
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            QuizQuestion question = quiz.getQuestions().get(i);

        }

    }
}
