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
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/socket/phone/{quizid}/{playerDetails}", encoders = JacksonEncoder.class)
public class PhoneSocket {

    private final QuizRunnerService quizRunnerService;
    private final Map<String, Map<PlayerDetails, Session>> sessions;
    private final ObjectMapper objectMapper;

    @Inject
    public PhoneSocket(QuizRunnerService quizRunnerService) {
        this.quizRunnerService = quizRunnerService;
        sessions = new ConcurrentHashMap<>();
        objectMapper = new ObjectMapper();
    }

    public void sendObject(String quizId, ClientObject obj) {
        sessions.getOrDefault(quizId, Collections.emptyMap())
                .keySet()
                .forEach(playerDetails -> sendObject(quizId, playerDetails, obj));
    }

    public void sendObject(String quizId, PlayerDetails playerDetails, ClientObject obj) {
        Optional.ofNullable(
                sessions.getOrDefault(quizId, Collections.emptyMap())
                        .get(playerDetails)
        )
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
        sessions.keySet().forEach(this::close);
    }

    public void close(String quizId) {
        sessions.getOrDefault(quizId, Collections.emptyMap()).forEach((playerDetails, session) -> close(quizId, playerDetails));
    }

    public void close(String quizId, PlayerDetails playerDetails) {
        Optional.ofNullable(sessions.getOrDefault(quizId, Collections.emptyMap()).get(playerDetails))
                .ifPresent(session -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("quizid") String quizid, @PathParam("playerDetails") String playerDetailsB64) {
        PlayerDetails playerDetails = b64JsonToObject(playerDetailsB64, PlayerDetails.class);
        sessions.putIfAbsent(quizid, new HashMap<>());
        sessions.get(quizid).put(playerDetails, session);
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
        switch (message.getType()) {
            case "fakeanswer":
                quizRunnerService.addFakeAnswer(quizid, playerDetails, message.getAnswer());
                break;
            case "answer":
                quizRunnerService.addAnswer(quizid, playerDetails, message.getAnswer());
                break;
            case "reload":
                quizRunnerService.resendPhoneStatus(quizid, playerDetails);
                break;
            default:
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
