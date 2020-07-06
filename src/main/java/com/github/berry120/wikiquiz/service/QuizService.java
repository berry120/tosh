package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.model.QuizState;
import com.github.berry120.wikiquiz.socket.DisplaySocket;
import com.github.berry120.wikiquiz.socket.PhoneSocket;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class QuizService {

    private final QuizIdGenerator idGenerator;
    private final DisplaySocket displaySocket;
    private final PhoneSocket phoneSocket;
    private final Map<String, QuizState> quizStates;

    @Inject
    QuizService(QuizIdGenerator idGenerator, DisplaySocket displaySocket, PhoneSocket phoneSocket) {
        this.idGenerator = idGenerator;
        this.displaySocket = displaySocket;
        this.phoneSocket = phoneSocket;
        quizStates = new HashMap<>();
    }

    public String createQuiz() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion("Cat is correct?", "Cat", List.of("Dog", "Hen", "Horse")));
        questions.add(new QuizQuestion("Dog is correct?", "Dog", List.of("Cat", "Hen", "Horse")));
        questions.add(new QuizQuestion("Hen is correct?", "Hen", List.of("Cat", "Dog", "Horse")));
        questions.add(new QuizQuestion("Horse is correct?", "Horse", List.of("Cat", "Dog", "Hen")));

        Quiz quiz = new Quiz(idGenerator.generateRandomId(), questions);
        quizStates.put(quiz.getId(), new QuizState(idGenerator, quiz));
        return quiz.getId();
    }

    public void startQuiz(String quizId) {
        if (!quizStates.get(quizId).isStarted()) {
            nextQuestion(quizId);
        }
    }

    public void addQuizAnswer(String quizId, String personId, String answer) {
        quizStates.get(quizId).addAnswer(personId, answer);
        if (quizStates.get(quizId).hasEveryoneAnswered()) {
            questionFinished(quizId);
        }
    }

    public void nextQuestion(String quizId) {
        if (quizStates.get(quizId).isFinished()) {
            displaySocket.sendObject(quizId, quizStates.get(quizId).getResults().toClientResults());
            phoneSocket.sendObject(quizId, quizStates.get(quizId).getResults().toClientResults());
        } else {
            quizStates.get(quizId).nextQuestion();
            displaySocket.sendObject(quizId, quizStates.get(quizId).getCurrentQuestion().toClientQuestion());
            phoneSocket.sendObject(quizId, quizStates.get(quizId).getCurrentQuestion().toClientQuestion());
        }
    }

    public void questionFinished(String quizId) {
        displaySocket.sendObject(quizId, quizStates.get(quizId).getCurrentQuestion().toClientAnswer());
    }

    public String addPersonToQuiz(String quizId, String name) {
        System.out.println(quizStates);
        return quizStates.get(quizId).addPlayer(name);
    }

}
