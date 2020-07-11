package com.github.berry120.wikiquiz.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.OutputStream;

@ApplicationScoped
public class QRCodeService {

    @ConfigProperty(name = "hostname")
    String hostname;


    public void generateQRCodeImage(String quizId, OutputStream outputStream) throws IOException {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = barcodeWriter.encode("http://" + hostname + "/phone/" + quizId, BarcodeFormat.QR_CODE, 1600, 1600);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

}
