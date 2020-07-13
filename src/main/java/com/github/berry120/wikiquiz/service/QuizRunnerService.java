package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizRunnerService {

    private final DisplaySocket displaySocket;
    private final PhoneSocket phoneSocket;
    private final RootSocket rootSocket;
    private final QuizStateService quizStateService;
    private final AnswerTransformerService answerTransformer;

    @Inject
    QuizRunnerService(QuizStateService quizStateService, DisplaySocket displaySocket, PhoneSocket phoneSocket, RootSocket rootSocket, AnswerTransformerService answerTransformer) {
        this.quizStateService = quizStateService;
        this.displaySocket = displaySocket;
        this.phoneSocket = phoneSocket;
        this.rootSocket = rootSocket;
        this.answerTransformer = answerTransformer;
    }

    public void startQuiz(String quizId) {
        nextQuestionOrFinish(quizId);
    }

    public boolean quizExists(String quizId) {
        return quizStateService.quizExists(quizId);
    }

    public Quiz getQuiz(String quizId) {
        return quizStateService.getQuiz(quizId);
    }

    public void addFakeAnswer(String quizId, String playerId, String fakeAnswer) {
        quizStateService.storeFakeAnswer(quizId, playerId, fakeAnswer);
        if (quizStateService.haveAllFakeAnswers(quizId)) {
            sendQuestionStage(quizId);
        }
    }

    public void addAnswer(String quizId, String playerId, String answer) {
        quizStateService.storeAnswer(quizId, playerId, answer);
        if (quizStateService.haveAllAnswers(quizId)) {
            sendResultsStage(quizId);
        }
    }

    public void nextQuestionOrFinish(String quizId) {
        int questionNumber = quizStateService.getQuestionNumber(quizId);
        if (questionNumber + 1 >= quizStateService.getQuiz(quizId).getQuestions().size()) {
            sendFinalScoreStage(quizId);
        } else {
            sendFakeQuestionStage(quizId);
        }
    }

    private void sendFakeQuestionStage(String quizId) {
        quizStateService.removeExistingAnswers(quizId);
        int questionNumber = quizStateService.getQuestionNumber(quizId) + 1;
        quizStateService.setQuestionNumber(quizId, questionNumber);

        Quiz quiz = quizStateService.getQuiz(quizId);
        String questionText = quiz.getQuestions().get(questionNumber).getQuestion();
        ClientFakeAnswerRequest clientFakeAnswerRequest = new ClientFakeAnswerRequest(questionText, questionNumber);

        displaySocket.sendObject(quizId, clientFakeAnswerRequest);
        phoneSocket.sendObject(quizId, clientFakeAnswerRequest);
    }

    public void sendQuestionStage(String quizId) {
        Quiz quiz = quizStateService.getQuiz(quizId);
        QuizQuestion question = quiz.getQuestions().get(quizStateService.getQuestionNumber(quizId));
        Set<String> questionOptions = new HashSet<>(quizStateService.getFakeAnswers(quizId).values());
        for (String sampleWrongAnswer : question.getSampleWrongAnswers()) {
            if (questionOptions.size() < 3) {
                questionOptions.add(sampleWrongAnswer);
            }
        }
        questionOptions.add(question.getCorrectAnswer());

        ClientQuestion clientQuestion = new ClientQuestion(question.getQuestion(), questionOptions);

        displaySocket.sendObject(quizId, clientQuestion);
        phoneSocket.sendObject(quizId, clientQuestion);
    }

    public void sendResultsStage(String quizId) {
        quizStateService.updateScores(quizId);

        int questionIdx = quizStateService.getQuestionNumber(quizId);
        QuizQuestion question = quizStateService.getQuiz(quizId).getQuestions().get(questionIdx);

        ClientAnswer clientAnswer = new ClientAnswer(question.getCorrectAnswer(),
                questionIdx,
                answerTransformer.answersToClientFormat(quizStateService.getAnswers(quizId)),
                answerTransformer.answersToClientFormat(quizStateService.getFakeAnswers(quizId)),
                quizStateService.getScores(quizId));

        displaySocket.sendObject(quizId, clientAnswer);
        phoneSocket.sendObject(quizId, clientAnswer);
    }

    private void sendFinalScoreStage(String quizId) {
        ClientResult clientResult = new ClientResult(quizStateService.getScores(quizId)
                .entrySet().stream()
                .map(e -> new ClientScore(e.getKey().getName(), e.getValue()))
                .collect(Collectors.toList()));

        displaySocket.sendObject(quizId, clientResult);
        phoneSocket.sendObject(quizId, clientResult);
    }

    public String addPlayer(String quizId, String playerName) {
        String id = quizStateService.addPlayer(quizId, playerName);
        ClientPlayerJoined clientPlayerJoined = new ClientPlayerJoined(playerName);
        rootSocket.sendObject(quizId, clientPlayerJoined);

        return id;
    }

}
