package com.github.berry120.wikiquiz.model;

import com.github.berry120.wikiquiz.model.client.PersonScore;
import com.github.berry120.wikiquiz.service.QuizIdGenerator;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class QuizState {

    private Map<String, Person> players;
    private QuizQuestion currentQuestion;
    private Map<QuizQuestion, Map<Person, String>> answers;
    private QuizIdGenerator idGenerator;
    private Quiz quiz;
    private int questionIdx;

    public QuizState(QuizIdGenerator idGenerator, Quiz quiz) {
        this.idGenerator = idGenerator;
        this.quiz = quiz;
        players = new HashMap<>();
        answers = new HashMap<>();
    }

    public String addPlayer(String name) {
        String id = idGenerator.generateRandomId();
        Person player = new Person(name, id);
        players.put(player.getId(), player);
        return id;
    }

    public boolean isStarted() {
        return questionIdx > 0;
    }

    public void nextQuestion() {
        if (questionIdx < quiz.getQuestions().size()) {
            this.currentQuestion = quiz.getQuestions().get(questionIdx++);
            answers.put(currentQuestion, new HashMap<>());
        } else {
            throw new RuntimeException("Quiz finished");
        }
    }

    public QuizResults getResults() {
        System.out.println(answers);
        return new QuizResults(
                players.values().stream()
                        .map(person -> new PersonScore(person.getName(), answers.entrySet().stream()
                                .flatMap(e -> e.getValue().entrySet().stream()
                                        .filter(p -> p.getKey().equals(person))
                                        .filter(p -> p.getValue().equals(e.getKey().getCorrectAnswer()))
                                        .map(Map.Entry::getKey)
                                ).count())
                        ).collect(Collectors.toList())
        );
    }

    public boolean isFinished() {
        return questionIdx >= quiz.getQuestions().size();
    }

    public void addAnswer(String personId, String answer) {
        answers.get(currentQuestion).putIfAbsent(players.get(personId), answer);
    }

    public boolean hasEveryoneAnswered() {
        return answers.get(currentQuestion).size() >= players.size();
    }

}
