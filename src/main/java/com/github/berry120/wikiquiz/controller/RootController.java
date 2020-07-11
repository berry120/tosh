package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.service.QuizService;
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

    private final QuizService quizService;
    private final Template root;

    @Inject
    RootController(QuizService quizService, Template root) {
        this.quizService = quizService;
        this.root = root;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response root() {
        String quizid = quizService.createQuiz();
        return Response
                .temporaryRedirect(URI.create("/" + quizid))
                .build();
    }

    @GET
    @Path("/{quizid}")
    @Produces(MediaType.TEXT_HTML)
    public Object root(@PathParam("quizid") String quizid) {
        if (quizService.quizExists(quizid)) {
            return root
                    .data("quizid", quizid);
        } else {
            return Response
                    .temporaryRedirect(URI.create("/"))
                    .build();
        }
    }

}