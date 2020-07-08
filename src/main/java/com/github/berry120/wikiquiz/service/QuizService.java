package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.model.QuizState;
import com.github.berry120.wikiquiz.socket.DisplaySocket;
import com.github.berry120.wikiquiz.socket.PhoneSocket;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class QuizService {

    private final QuizIdGenerator idGenerator;
    private final DisplaySocket displaySocket;
    private final PhoneSocket phoneSocket;
    private final QuizState quizState;

    @Inject
    QuizService(QuizIdGenerator idGenerator, QuizState quizState, DisplaySocket displaySocket, PhoneSocket phoneSocket) {
        this.idGenerator = idGenerator;
        this.quizState = quizState;
        this.displaySocket = displaySocket;
        this.phoneSocket = phoneSocket;
    }

    public String createQuiz() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion("Cat is correct?", "Cat", List.of("Dog", "Hen", "Horse")));
        questions.add(new QuizQuestion("Dog is correct?", "Dog", List.of("Cat", "Hen", "Horse")));
        questions.add(new QuizQuestion("Hen is correct?", "Hen", List.of("Cat", "Dog", "Horse")));
        questions.add(new QuizQuestion("Horse is correct?", "Horse", List.of("Cat", "Dog", "Hen")));

        Quiz quiz = new Quiz(idGenerator.generateRandomId(), questions);
        quizState.addQuiz(quiz);
        return quiz.getId();
    }

    public void startQuiz(String quizId) {
        if (!quizState.isStarted(quizId)) {
            nextQuestion(quizId);
        }
    }

    public void addQuizAnswer(String quizId, String personId, String answer) {
        quizState.addAnswer(quizId, personId, answer);
        if (quizState.hasEveryoneAnswered(quizId)) {
            questionFinished(quizId);
        }
    }

    public void nextQuestion(String quizId) {
        if (quizState.isFinished(quizId)) {
            displaySocket.sendObject(quizId, quizState.getResults(quizId).toClientResults());
            phoneSocket.sendObject(quizId, quizState.getResults(quizId).toClientResults());
        } else {
            quizState.advanceQuestion(quizId);
            displaySocket.sendObject(quizId, quizState.getCurrentQuestion(quizId).toClientQuestion());
            phoneSocket.sendObject(quizId, quizState.getCurrentQuestion(quizId).toClientQuestion());
        }
    }

    public void questionFinished(String quizId) {
        displaySocket.sendObject(quizId, quizState.getCurrentQuestion(quizId).toClientAnswer());
    }

    public String addPersonToQuiz(String quizId, String name) {
        return quizState.addPlayer(quizId, name);
    }

}
