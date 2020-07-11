package com.github.berry120.wikiquiz.controller;

import com.github.berry120.wikiquiz.service.QRCodeService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

@Path("/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @Inject
    QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GET
    @Path("/{content}")
    @Produces("image/png")
    public StreamingOutput mainDisplay(@PathParam String content) {
        return outputStream -> qrCodeService.generateQRCodeImage(content, outputStream);
    }
}
