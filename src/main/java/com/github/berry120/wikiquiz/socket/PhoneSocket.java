package com.github.berry120.wikiquiz.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.service.QuizRunnerService;
import com.github.berry120.wikiquiz.util.JacksonEncoder;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/socket/phone/{quizid}/{personid}", encoders = JacksonEncoder.class)
public class PhoneSocket {

    private final QuizRunnerService quizRunnerService;
    private final Map<String, List<Session>> sessions;
    private final ObjectMapper objectMapper;

    @Inject
    public PhoneSocket(QuizRunnerService quizRunnerService) {
        this.quizRunnerService = quizRunnerService;
        sessions = new ConcurrentHashMap<>();
        objectMapper = new ObjectMapper();
    }

    public void sendObject(String quizid, ClientObject obj) {
        sessions.getOrDefault(quizid, Collections.emptyList())
                .stream()
                .filter(Session::isOpen)
                .forEach(session ->
                        session.getAsyncRemote().sendObject(obj, result -> {
                            System.out.println("Sent ok: " + result.isOK());
                            if (!result.isOK()) result.getException().printStackTrace();
                        })
                );
    }

    @PreDestroy
    public void closeAll(String quizid) {
        sessions.getOrDefault(quizid, Collections.emptyList()).forEach(session -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("quizid") String quizid, @PathParam("personid") String personid) {
        sessions.putIfAbsent(quizid, new ArrayList<>());
        sessions.get(quizid).add(session);
    }

    @OnMessage
    public void onMessage(String rawMessage, @PathParam("quizid") String quizid, @PathParam("personid") String personid) {
        try {
            PhoneSocketMessage message = objectMapper.readValue(rawMessage, PhoneSocketMessage.class);
            if (message.getType().equals("fakeanswer")) {
                quizRunnerService.addFakeAnswer(quizid, personid, message.getAnswer());
            } else if (message.getType().equals("answer")) {
                quizRunnerService.addAnswer(quizid, personid, message.getAnswer());
            } else {
                throw new RuntimeException("Unknown type");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
