package com.github.berry120.wikiquiz.socket;

import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.service.QuizService;
import com.github.berry120.wikiquiz.util.JacksonEncoder;
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

    private final QuizService quizService;
    private final Map<String, List<Session>> sessions;

    @Inject
    public PhoneSocket(QuizService quizService) {
        this.quizService = quizService;
        sessions = new ConcurrentHashMap<>();
    }

    public void sendObject(String quizid, ClientObject obj) {
        sessions.getOrDefault(quizid, Collections.emptyList()).forEach(session ->
                session.getAsyncRemote().sendObject(obj, result -> {
                    System.out.println("Sent ok: " + result.isOK());
                    if (!result.isOK()) result.getException().printStackTrace();
                })
        );
    }

    public void close(String quizid) {
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
        System.out.println("Opened - " + quizid + " - " + personid);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("quizid") String quizid, @PathParam("personid") String personid) {
        quizService.addQuizAnswer(quizid, personid, message);
        System.out.println(quizid + " - " + personid + message);
    }

}
