package com.github.berry120.wikiquiz.socket;

import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.util.JacksonEncoder;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
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
@ServerEndpoint(value = "/socket/{quizid}", encoders = JacksonEncoder.class)
public class RootSocket {

    private final Map<String, List<Session>> sessions;

    public RootSocket() {
        sessions = new ConcurrentHashMap<>();
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
    public void closeAll() {
        sessions.values().forEach(list -> list.forEach(session -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        ));
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("quizid") String quizid) {
        sessions.putIfAbsent(quizid, new ArrayList<>());
        sessions.get(quizid).add(session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("quizid") String quizid) {
        sessions.getOrDefault(quizid, new ArrayList<>()).remove(session);
    }

}
