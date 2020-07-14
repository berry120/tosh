package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.model.client.PlayerDetails;
import com.github.berry120.wikiquiz.service.QuizRunnerService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/phone")
public class PhoneController {

    private final QuizRunnerService quizRunnerService;
    private final Template phoneStart;
    private final Template phoneQuiz;

    @Inject
    PhoneController(QuizRunnerService quizRunnerService,
                    @ResourcePath("phone_start") Template phoneStart,
                    @ResourcePath("phone_quiz") Template phoneQuiz) {
        this.quizRunnerService = quizRunnerService;
        this.phoneStart = phoneStart;
        this.phoneQuiz = phoneQuiz;
    }

    @GET
    @Path("/{quizId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance phoneStart(@PathParam String quizId) {
        return phoneStart
                .data("quizid", quizId);
    }

    @GET
    @Path("/{quizId}/{personId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance phoneQuiz(@PathParam String quizId, @PathParam String personId) {
        return phoneQuiz
                .data("quizid", quizId)
                .data("personid", personId);
    }

    @POST
    @Path("/{quizId}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean registerPerson(@PathParam String quizId, PlayerDetails playerDetails) {
        return quizRunnerService.addPlayer(quizId, playerDetails);
    }

}
