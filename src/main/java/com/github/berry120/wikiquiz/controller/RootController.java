package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.service.QuizCreationService;
import com.github.berry120.wikiquiz.service.QuizRunnerService;
import io.quarkus.qute.Template;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootController {

    private final QuizCreationService quizCreationService;
    private final QuizRunnerService quizRunnerService;
    private final Template root;

    @Inject
    RootController(QuizRunnerService quizRunnerService, QuizCreationService quizCreationService, Template root) {
        this.quizRunnerService = quizRunnerService;
        this.quizCreationService = quizCreationService;
        this.root = root;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response root() {
        Quiz quiz = quizCreationService.createQuiz();
        return Response
                .temporaryRedirect(URI.create("/" + quiz.getId()))
                .build();
    }

    @GET
    @Path("/{quizid}")
    @Produces(MediaType.TEXT_HTML)
    public Object root(@PathParam("quizid") String quizid) {
        if (quizRunnerService.quizExists(quizid)) {
            return root
                    .data("quizid", quizid);
        } else {
            return Response
                    .temporaryRedirect(URI.create("/"))
                    .build();
        }
    }

}