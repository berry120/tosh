package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.model.Quiz;
import com.github.berry120.wikiquiz.redis.RedisRepository;
import com.github.berry120.wikiquiz.service.QuizCreationService;
import com.github.berry120.wikiquiz.service.QuizRunnerService;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
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
    private final RedisRepository redisRepository;
    private final Template root;

    @Inject
    RootController(QuizRunnerService quizRunnerService,
                   QuizCreationService quizCreationService,
                   RedisRepository redisRepository,
                   @ResourcePath("root") Template root) {
        this.quizRunnerService = quizRunnerService;
        this.quizCreationService = quizCreationService;
        this.redisRepository = redisRepository;
        this.root = root;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response root() {
        Quiz quiz = quizCreationService.createQuiz();
        redisRepository.storeQuiz(quiz);
        return Response
                .temporaryRedirect(URI.create("/" + quiz.getId()))
                .build();
    }

    @GET
    @Path("/{quizid}")
    @Produces(MediaType.TEXT_HTML)
    public Object root(@PathParam("quizid") String quizId) {
        if (quizRunnerService.quizExists(quizId)) {
            return root
                    .data("players", redisRepository.retrievePlayers(quizId))
                    .data("quizid", quizId);
        } else {
            return Response
                    .temporaryRedirect(URI.create("/"))
                    .build();
        }
    }

}