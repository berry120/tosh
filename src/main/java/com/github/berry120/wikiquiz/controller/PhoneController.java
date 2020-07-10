package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.model.PlayerParameters;
import com.github.berry120.wikiquiz.model.QuizQuestion;
import com.github.berry120.wikiquiz.service.QuizService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Path("/phone")
public class PhoneController {

    private final QuizService quizService;
    private final Template phonestart;
    private final Template phonequiz;

    @Inject
    PhoneController(QuizService quizService, Template phonestart, Template phonequiz) {
        this.quizService = quizService;
        this.phonestart = phonestart;
        this.phonequiz = phonequiz;
    }

    @GET
    @Path("/{quizId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance phoneStart(@PathParam String quizId) {
        return phonestart
                .data("quizid", quizId)
                .data("questions", quizService.getQuiz(quizId).getQuestions().stream()
                        .map(QuizQuestion::getQuestion).collect(Collectors.toList()));
    }

    @GET
    @Path("/{quizId}/{personId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance phoneQuiz(@PathParam String quizId, @PathParam String personId) {
        return phonequiz
                .data("quizid", quizId)
                .data("personid", personId);
    }

    @POST
    @Path("/{quizId}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String registerPerson(@PathParam String quizId, PlayerParameters playerParameters) {
        return quizService.addPlayer(quizId, playerParameters.getPlayerName());
    }

}
