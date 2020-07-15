package com.github.berry120.wikiquiz.service;

import com.github.berry120.wikiquiz.model.QuestionStage;
import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.model.QuizState;
import com.github.berry120.wikiquiz.model.client.ClientAnswer;
import com.github.berry120.wikiquiz.model.client.ClientFakeAnswerRequest;
import com.github.berry120.wikiquiz.model.client.ClientPlayerJoined;
import com.github.berry120.wikiquiz.model.client.ClientPlayerRemoved;
import com.github.berry120.wikiquiz.model.client.ClientQuestion;
import com.github.berry120.wikiquiz.model.client.ClientResult;
import com.github.berry120.wikiquiz.model.client.ClientScore;
import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import com.github.berry120.wikiquiz.redis.RedisRepository;
import com.github.berry120.wikiquiz.socket.DisplaySocket;
import com.github.berry120.wikiquiz.socket.PhoneSocket;
import com.github.berry120.wikiquiz.socket.RootSocket;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuizRunnerService {

    private final DisplaySocket displaySocket;
    private final PhoneSocket phoneSocket;
    private final RootSocket rootSocket;
    private final RedisRepository redisRepository;
    private final AnswerTransformerService answerTransformer;

    @Inject
    QuizRunnerService(RedisRepository redisRepository, DisplaySocket displaySocket, PhoneSocket phoneSocket, RootSocket rootSocket, AnswerTransformerService answerTransformer) {
        this.redisRepository = redisRepository;
        this.displaySocket = displaySocket;
        this.phoneSocket = phoneSocket;
        this.rootSocket = rootSocket;
        this.answerTransformer = answerTransformer;
    }

    public void startQuiz(String quizId) {
        if (redisRepository.retrieveQuizState(quizId).getQuestionNumber() == -1) {
            nextQuestionOrFinish(quizId);
        }
    }

    public boolean quizExists(String quizId) {
        return redisRepository.quizExists(quizId);
    }

    public void addFakeAnswer(String quizId, PlayerDetails playerDetails, String fakeAnswer) {
        redisRepository.storeFakeAnswer(quizId, playerDetails, fakeAnswer);
        if (redisRepository.haveAllFakeAnswers(quizId)) {
            sendQuestionStage(quizId);
        }
    }

    public void addAnswer(String quizId, PlayerDetails playerDetails, String answer) {
        redisRepository.storeAnswer(quizId, playerDetails, answer);
        if (redisRepository.haveAllAnswers(quizId)) {
            sendResultsStage(quizId);
        }
    }

    public void resendDisplayStatus(String quizId) {
        QuizState quizState = redisRepository.retrieveQuizState(quizId).rewind();
        redisRepository.storeQuizState(quizId, quizState);

        if (quizState.getQuestionStage() == QuestionStage.FAKE_ANSWER_SUBMISSION) {
            sendQuestionStage(quizId);
        } else if (quizState.getQuestionStage() == QuestionStage.PICK_ANSWER) {
            sendResultsStage(quizId);
        } else if (quizState.getQuestionStage() == QuestionStage.SHOW_RESULTS) {
            nextQuestionOrFinish(quizId);
        }
    }

    public void resendPhoneStatus(String quizId, PlayerDetails playerDetails) {
        Quiz quiz = redisRepository.retrieveQuiz(quizId);
        QuizState quizState = redisRepository.retrieveQuizState(quizId);
        if (quizState.isStarted()) {
            QuizQuestion question = quiz.getQuestions().get(quizState.getQuestionNumber());

            if (quizState.getQuestionStage() == QuestionStage.FAKE_ANSWER_SUBMISSION) {
                ClientFakeAnswerRequest clientFakeAnswerRequest = new ClientFakeAnswerRequest(question.getQuestion(), quizState.getQuestionNumber());
                phoneSocket.sendObject(quizId, playerDetails, clientFakeAnswerRequest);
            } else if (quizState.getQuestionStage() == QuestionStage.PICK_ANSWER) {
                ClientQuestion clientQuestion = new ClientQuestion(question.getQuestion(), getQuestionOptions(quiz, question));
                phoneSocket.sendObject(quizId, playerDetails, clientQuestion);
            } else if (quizState.getQuestionStage() == QuestionStage.SHOW_RESULTS) {
                ClientAnswer clientAnswer = getClientAnswer(quizState, quiz, question);
                phoneSocket.sendObject(quizId, playerDetails, clientAnswer);
            }
        }
    }

    public void nextQuestionOrFinish(String quizId) {
        int questionNumber = redisRepository.retrieveQuizState(quizId).getQuestionNumber();
        if (questionNumber + 1 >= redisRepository.retrieveQuiz(quizId).getQuestions().size()) {
            sendFinalScoreStage(quizId);
        } else {
            sendFakeQuestionStage(quizId);
        }
    }

    private void sendFakeQuestionStage(String quizId) {
        QuizState quizState = redisRepository.retrieveQuizState(quizId);
        if (quizState.getQuestionStage() == QuestionStage.SHOW_RESULTS) {
            int questionNumber = quizState.getQuestionNumber() + 1;
            redisRepository.storeQuizState(quizId, new QuizState(questionNumber, QuestionStage.FAKE_ANSWER_SUBMISSION));

            redisRepository.removeTempQuestionData(quizId);
            Quiz quiz = redisRepository.retrieveQuiz(quizId);
            String questionText = quiz.getQuestions().get(questionNumber).getQuestion();
            ClientFakeAnswerRequest clientFakeAnswerRequest = new ClientFakeAnswerRequest(questionText, questionNumber);

            displaySocket.sendObject(quizId, clientFakeAnswerRequest);
            phoneSocket.sendObject(quizId, clientFakeAnswerRequest);
        }
    }

    public void sendQuestionStage(String quizId) {
        QuizState quizState = redisRepository.retrieveQuizState(quizId);
        if (quizState.getQuestionStage() == QuestionStage.FAKE_ANSWER_SUBMISSION) {
            redisRepository.storeQuizState(quizId, new QuizState(quizState.getQuestionNumber(), QuestionStage.PICK_ANSWER));

            Quiz quiz = redisRepository.retrieveQuiz(quizId);
            QuizQuestion question = quiz.getQuestions().get(quizState.getQuestionNumber());

            ClientQuestion clientQuestion = new ClientQuestion(question.getQuestion(), getQuestionOptions(quiz, question));

            displaySocket.sendObject(quizId, clientQuestion);
            phoneSocket.sendObject(quizId, clientQuestion);
        }
    }

    private List<String> getQuestionOptions(Quiz quiz, QuizQuestion question) {
        Set<String> questionOptions = new HashSet<>(redisRepository.retrieveFakeAnswers(quiz.getId()).values());
        for (String sampleWrongAnswer : question.getSampleWrongAnswers()) {
            if (questionOptions.size() < 3) {
                questionOptions.add(sampleWrongAnswer);
            }
        }
        questionOptions.add(question.getCorrectAnswer());
        return questionOptions.stream().sorted().collect(Collectors.toList());
    }

    public void sendResultsStage(String quizId) {
        QuizState quizState = redisRepository.retrieveQuizState(quizId);
        if (quizState.getQuestionStage() == QuestionStage.PICK_ANSWER) {
            redisRepository.storeQuizState(quizId, new QuizState(quizState.getQuestionNumber(), QuestionStage.SHOW_RESULTS));
            redisRepository.updateScores(quizId);

            Quiz quiz = redisRepository.retrieveQuiz(quizId);
            QuizQuestion question = quiz.getQuestions().get(quizState.getQuestionNumber());

            ClientAnswer clientAnswer = getClientAnswer(quizState, quiz, question);

            displaySocket.sendObject(quizId, clientAnswer);
            phoneSocket.sendObject(quizId, clientAnswer);
        }
    }

    private ClientAnswer getClientAnswer(QuizState quizState, Quiz quiz, QuizQuestion question) {
        return new ClientAnswer(
                quizState.getQuestionNumber(),
                question.getQuestion(),
                question.getCorrectAnswer(),
                getQuestionOptions(quiz, question),
                answerTransformer.answersToClientFormat(redisRepository.retrieveAnswers(quiz.getId()).entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue))),
                answerTransformer.answersToClientFormat(redisRepository.retrieveFakeAnswers(quiz.getId()).entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue))),
                redisRepository.retrieveScores(quiz.getId())
        );
    }

    private void sendFinalScoreStage(String quizId) {
        ClientResult clientResult = new ClientResult(redisRepository.retrieveScores(quizId)
                .entrySet().stream()
                .map(e -> new ClientScore(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        displaySocket.sendObject(quizId, clientResult);
        phoneSocket.sendObject(quizId, clientResult);
    }

    public boolean addPlayer(String quizId, PlayerDetails playerDetails) {
        boolean storedOk = redisRepository.storePlayer(quizId, playerDetails);
        if (storedOk) {
            rootSocket.sendObject(quizId, new ClientPlayerJoined(playerDetails.getName()));
        }
        return storedOk;
    }

    public void removePlayer(String quizId, PlayerDetails playerDetails) {
        redisRepository.removePlayer(quizId, playerDetails);
        rootSocket.sendObject(quizId, new ClientPlayerRemoved(playerDetails.getName()));
    }

}
