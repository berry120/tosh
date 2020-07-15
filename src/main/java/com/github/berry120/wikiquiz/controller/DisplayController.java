package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.redis.RedisRepository;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/display")
public class DisplayController {

    private final RedisRepository redisRepository;
    private final Template displayQuiz;

    @Inject
    DisplayController(RedisRepository redisRepository, @ResourcePath("display_quiz") Template displayQuiz) {
        this.redisRepository = redisRepository;
        this.displayQuiz = displayQuiz;
    }

    @GET
    @Path("/{quizId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance mainDisplay(@PathParam String quizId) {
        return displayQuiz
                .data("quizid", quizId)
                .data("initialQuestionNum", redisRepository.retrieveQuizState(quizId).getQuestionNumber());
    }

}
