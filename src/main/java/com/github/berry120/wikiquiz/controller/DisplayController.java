package com.github.berry120.wikiquiz.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/display")
public class DisplayController {
    private final Template displayquiz;

    @Inject
    DisplayController(Template displayquiz) {
        this.displayquiz = displayquiz;
    }

    @GET()
    @Path("/{quizId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance mainDisplay(@PathParam String quizId) {
        return displayquiz
                .data("quizid", quizId);
    }

}
