package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.model.QuizState;
import com.github.berry120.wikiquiz.model.client.ClientAnswer;
import com.github.berry120.wikiquiz.model.client.ClientFakeAnswerRequest;
import com.github.berry120.wikiquiz.model.client.ClientPlayerJoined;
import com.github.berry120.wikiquiz.model.client.ClientQuestion;
import com.github.berry120.wikiquiz.model.client.ClientResult;
import com.github.berry120.wikiquiz.model.client.ClientScore;
import com.github.berry120.wikiquiz.socket.DisplaySocket;
import com.github.berry120.wikiquiz.socket.PhoneSocket;
import com.github.berry120.wikiquiz.socket.RootSocket;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizService {

    private final RandomIdGenerator idGenerator;
    private final DisplaySocket displaySocket;
    private final PhoneSocket phoneSocket;
    private final RootSocket rootSocket;
    private final QuizState quizState;

    @Inject
    QuizService(RandomIdGenerator idGenerator, QuizState quizState, DisplaySocket displaySocket, PhoneSocket phoneSocket, RootSocket rootSocket) {
        this.idGenerator = idGenerator;
        this.quizState = quizState;
        this.displaySocket = displaySocket;
        this.phoneSocket = phoneSocket;
        this.rootSocket = rootSocket;
    }

    public String createQuiz() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion("Cat is correct?", "Cat"));
        questions.add(new QuizQuestion("Dog is correct?", "Dog"));
        questions.add(new QuizQuestion("Hen is correct?", "Hen"));
        questions.add(new QuizQuestion("Horse is correct?", "Horse"));

        Quiz quiz = new Quiz(idGenerator.generateRandomId(), questions);
        quizState.createQuiz(quiz);
        return quiz.getId();
    }

    public void startQuiz(String quizId) {
        nextQuestionOrFinish(quizId);
    }

    public boolean quizExists(String quizId) {
        return quizState.quizExists(quizId);
    }

    public Quiz getQuiz(String quizId) {
        return quizState.getQuiz(quizId);
    }

    public void addFakeAnswer(String quizId, String playerId, String fakeAnswer) {
        quizState.storeFakeAnswer(quizId, playerId, fakeAnswer);
        if (quizState.haveAllFakeAnswers(quizId)) {
            sendQuestionStage(quizId);
        }
    }

    public void addAnswer(String quizId, String playerId, String answer) {
        quizState.storeAnswer(quizId, playerId, answer);
        if (quizState.haveAllAnswers(quizId)) {
            sendResultsStage(quizId);
        }
    }

    public void nextQuestionOrFinish(String quizId) {
        int questionNumber = quizState.getQuestionNumber(quizId);
        if (questionNumber + 1 >= quizState.getQuiz(quizId).getQuestions().size()) {
            sendFinalScoreStage(quizId);
        } else {
            sendFakeQuestionStage(quizId);
        }
    }

    private void sendFakeQuestionStage(String quizId) {
        quizState.removeExistingAnswers(quizId);
        int questionNumber = quizState.getQuestionNumber(quizId) + 1;
        quizState.setQuestionNumber(quizId, questionNumber);

        Quiz quiz = quizState.getQuiz(quizId);
        String questionText = quiz.getQuestions().get(questionNumber).getQuestion();
        ClientFakeAnswerRequest clientFakeAnswerRequest = new ClientFakeAnswerRequest(questionText, questionNumber);

        displaySocket.sendObject(quizId, clientFakeAnswerRequest);
        phoneSocket.sendObject(quizId, clientFakeAnswerRequest);
    }

    public void sendQuestionStage(String quizId) {
        Quiz quiz = quizState.getQuiz(quizId);
        QuizQuestion question = quiz.getQuestions().get(quizState.getQuestionNumber(quizId));
        List<String> questionOptions = new ArrayList<>(quizState.getFakeAnswers(quizId).values());
        questionOptions.add(question.getCorrectAnswer());

        ClientQuestion clientQuestion = new ClientQuestion(question.getQuestion(), questionOptions);

        displaySocket.sendObject(quizId, clientQuestion);
        phoneSocket.sendObject(quizId, clientQuestion);
    }

    public void sendResultsStage(String quizId) {
        quizState.updateScores(quizId);

        QuizQuestion question = quizState.getQuiz(quizId).getQuestions().get(quizState.getQuestionNumber(quizId));

        ClientAnswer clientAnswer = new ClientAnswer(question.getCorrectAnswer(),
                quizState.getAnswers(quizId),
                quizState.getFakeAnswers(quizId),
                quizState.getScores(quizId));

        displaySocket.sendObject(quizId, clientAnswer);
        phoneSocket.sendObject(quizId, clientAnswer);
    }

    private void sendFinalScoreStage(String quizId) {
        ClientResult clientResult = new ClientResult(quizState.getScores(quizId)
                .entrySet().stream()
                .map(e -> new ClientScore(e.getKey().getName(), e.getValue()))
                .collect(Collectors.toList()));

        displaySocket.sendObject(quizId, clientResult);
        phoneSocket.sendObject(quizId, clientResult);
    }

    public String addPlayer(String quizId, String playerName) {
        String id = quizState.addPlayer(quizId, playerName);
        ClientPlayerJoined clientPlayerJoined = new ClientPlayerJoined(playerName);
        rootSocket.sendObject(quizId, clientPlayerJoined);

        return id;
    }

}
