package com.github.berry120.wikiquiz.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.berry120.wikiquiz.model.client.ClientObject;
import com.github.berry120.wikiquiz.model.client.PlayerDetails;
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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/socket/phone/{quizid}/{playerDetails}", encoders = JacksonEncoder.class)
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
    public void onOpen(Session session, @PathParam("quizid") String quizid) {
        sessions.putIfAbsent(quizid, new ArrayList<>());
        sessions.get(quizid).add(session);
    }

//    @OnClose
//    public void onClose(Session session, @PathParam("quizid") String quizid, @PathParam("playerDetails") String playerDetailsB64) {
//        PlayerDetails playerDetails = b64JsonToObject(playerDetailsB64, PlayerDetails.class);
//        sessions.get(quizid).remove(session);
//        quizRunnerService.removePlayer(quizid, playerDetails);
//    }

    @OnMessage
    public void onMessage(String rawMessage, @PathParam("quizid") String quizid, @PathParam("playerDetails") String playerDetailsB64) {
        PlayerDetails playerDetails = b64JsonToObject(playerDetailsB64, PlayerDetails.class);
        PhoneSocketMessage message = jsonToObject(rawMessage, PhoneSocketMessage.class);
        if (message.getType().equals("fakeanswer")) {
            quizRunnerService.addFakeAnswer(quizid, playerDetails, message.getAnswer());
        } else if (message.getType().equals("answer")) {
            quizRunnerService.addAnswer(quizid, playerDetails, message.getAnswer());
        } else {
            throw new RuntimeException("Unknown type");
        }
    }

    private <T> T b64JsonToObject(String b64Json, Class<T> c) {
        return jsonToObject(new String(Base64.getDecoder().decode(b64Json), StandardCharsets.UTF_8), c);
    }

    private <T> T jsonToObject(String json, Class<T> c) {
        try {
            return objectMapper.readValue(json, c);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
