package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.service.QuizService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class RootController {

    private final QuizService quizService;
    private final Template root;

    @Inject
    RootController(QuizService quizService, Template root) {
        this.quizService = quizService;
        this.root = root;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance root() {
        String quizid = quizService.createQuiz();
        return root
                .data("quizid", quizid);
    }

}