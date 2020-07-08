package com.github.berry120.wikiquiz.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.berry120.wikiquiz.model.client.PersonScore;
import com.github.berry120.wikiquiz.redis.AnswerKey;
import com.github.berry120.wikiquiz.redis.PlayerKey;
import com.github.berry120.wikiquiz.redis.QuestionPositionKey;
import com.github.berry120.wikiquiz.redis.QuizKey;
import com.github.berry120.wikiquiz.service.QuizIdGenerator;
import com.github.berry120.wikiquiz.service.RedisService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizState {

    private final QuizIdGenerator idGenerator;
    private final RedisService redisService;

    @Inject
    QuizState(QuizIdGenerator idGenerator, RedisService redisService) {
        this.idGenerator = idGenerator;
        this.redisService = redisService;
    }

    public String addPlayer(String quizId, String name) {
        String playerId = idGenerator.generateRandomId();
        Person player = new Person(name, playerId);

        Map<String, Person> players = redisService.get(new PlayerKey(quizId), new TypeReference<Map<String, Person>>() {
        }).orElse(new HashMap<>() {
        });
        if (players == null) {
            players = new HashMap<>() {
            };
        }
        players.put(playerId, player);

        redisService.set(new PlayerKey(quizId), players);
        return playerId;
    }

    public void addQuiz(Quiz quiz) {
        String id = quiz.getId();
        redisService.set(new QuizKey(id), quiz);
    }

    public boolean isStarted(String quizId) {
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(0);
        return questionPosition > 0;
    }

    public QuizQuestion getCurrentQuestion(String quizId) {
        Quiz quiz = redisService.get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).get();
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(0);

        return quiz.getQuestions().get(questionPosition);
    }

    public void advanceQuestion(String quizId) {
        Quiz quiz = redisService.get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).get();
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(-1);

        if (questionPosition < quiz.getQuestions().size()) {
            redisService.set(new QuestionPositionKey(quizId), questionPosition + 1);
        } else {
            throw new RuntimeException("Quiz finished");
        }
    }

    public QuizResults getResults(String quizId) {
        Quiz quiz = redisService.get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).get();
        Map<String, Person> players = redisService.get(new PlayerKey(quizId), new TypeReference<Map<String, Person>>() {
        }).get();

        List<PersonScore> ret = quiz.getQuestions()
                .stream()
                .flatMap(q -> {
                    int questionIdx = quiz.getQuestions().indexOf(q);
                    AnswerKey key = new AnswerKey(quizId, questionIdx);
                    Map<String, QuizAnswer> answers = redisService.get(key, new TypeReference<Map<String, QuizAnswer>>() {
                    }).get();

                    return answers.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().isCorrect())
                            .map(entry -> entry.getKey());

                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> new PersonScore(players.get(e.getKey()).getName(), e.getValue()))
                .collect(Collectors.toList());

        return new QuizResults(ret);
    }

    public boolean isFinished(String quizId) {
        Quiz quiz = redisService.get(new QuizKey(quizId), new TypeReference<Quiz>() {
        }).get();
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(0);

        return questionPosition >= quiz.getQuestions().size() - 1;
    }

    public void addAnswer(String quizId, String personId, String answer) {
        QuizQuestion question = getCurrentQuestion(quizId);
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(0);
        AnswerKey key = new AnswerKey(quizId, questionPosition);

        Map<String, QuizAnswer> answersForQuestion = redisService.get(key, new TypeReference<Map<String, QuizAnswer>>() {
        }).orElse(new HashMap<>() {
        });
        answersForQuestion.putIfAbsent(personId, new QuizAnswer(personId, answer, question.getCorrectAnswer().equals(answer)));
        redisService.set(key, answersForQuestion);
    }

    public boolean hasEveryoneAnswered(String quizId) {
        int questionPosition = redisService.get(new QuestionPositionKey(quizId), new TypeReference<Integer>() {
        }).orElse(0);
        AnswerKey key = new AnswerKey(quizId, questionPosition);

        Map<String, QuizAnswer> answersForQuestion = redisService.get(key, new TypeReference<Map<String, QuizAnswer>>() {
        }).orElse(new HashMap<>() {
        });

        Map<String, Person> players = redisService.get(new PlayerKey(quizId), new TypeReference<Map<String, Person>>() {
        }).orElse(new HashMap<>() {
        });
        return answersForQuestion.size() >= players.size();
    }

}
