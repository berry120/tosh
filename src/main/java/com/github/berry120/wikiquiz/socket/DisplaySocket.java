package com.github.berry120.wikiquiz.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.service.QuizRunnerService;
import com.github.berry120.wikiquiz.util.JacksonEncoder;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/socket/display/{quizid}", encoders = JacksonEncoder.class)
public class DisplaySocket {

    private final QuizRunnerService quizRunnerService;
    private final Map<String, Session> sessions;
    private final ObjectMapper objectMapper;

    @Inject
    public DisplaySocket(QuizRunnerService quizRunnerService) {
        this.quizRunnerService = quizRunnerService;
        sessions = new ConcurrentHashMap<>();
        objectMapper = new ObjectMapper();
    }

    public void sendObject(String quizid, ClientObject obj) {
        sessions.get(quizid).getAsyncRemote().sendObject(obj, result -> {
            System.out.println("Sent ok: " + result.isOK());
            if (!result.isOK()) result.getException().printStackTrace();
        });
    }

    public void close(String quizid) {
        try {
            sessions.get(quizid).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("quizid") String quizid) {
        sessions.put(quizid, session);
        quizRunnerService.startQuiz(quizid);
    }

    @OnMessage
    public void onMessage(String rawMessage, @PathParam("quizid") String quizid) {
        try {
            DisplaySocketMessage message = objectMapper.readValue(rawMessage, DisplaySocketMessage.class);
            System.out.println(quizid + " - " + message);

            switch (message.getType()) {
                case "questionfinished":
                    quizRunnerService.sendResultsStage(quizid);
                    break;
                case "displayanswerfinished":
                    quizRunnerService.nextQuestionOrFinish(quizid);
                    break;
                case "fakeanswerfinished":
                    quizRunnerService.sendQuestionStage(quizid);
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
