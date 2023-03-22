package com.poc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MfaUtil {

    public static final String APP_NAME = "Veragi";
    public static final String EMAIL_ACCOUNT = "teste@testando.com";
    private static final String QR_CODE_IMAGE_ZXING_PATH = "./QRCodebyGoogle.png";
    private static final String QR_CODE_IMAGE_COMMONS_PATH = "./QRCodebyApache.png";

    public static final Logger LOG = LoggerFactory.getLogger(MfaUtil.class);

    public static String generateQRCodeImage(String secret, int size)
            throws IOException, WriterException {
        String uri = String.format("otpauth://totp/%1$s:%2$s?secret=%3$s&issuer=%1$s", APP_NAME, EMAIL_ACCOUNT, secret);

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix matrix = barcodeWriter.encode(uri, BarcodeFormat.QR_CODE, size, size);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

        Path path = FileSystems.getDefault().getPath(QR_CODE_IMAGE_ZXING_PATH);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static void main(String[] args) {
        try {
            String secret = Base32.random();
            String qrCodeImage = generateQRCodeImage(secret, 200);
            //TODO: PARA USAR EM UMA TAG HTML DE IMAGEM
            LOG.info("QRCODE = data:image/png;base64,{}", qrCodeImage);

            byte[] decodedQRCode = Base64.getDecoder().decode(qrCodeImage);
            FileUtils.writeByteArrayToFile(new File(QR_CODE_IMAGE_COMMONS_PATH), decodedQRCode);

            Totp mfa = new Totp(secret);
            Scanner scanner = new Scanner(System.in);
            int maxima = 5;
            for (int tentativa = 1; tentativa <= maxima; tentativa++) {
                LOG.info("Tentativa {} de {}. Informe o código:", tentativa, maxima);
                if (mfa.verify(scanner.nextLine())) {
                    LOG.info("Autenticação MFA bem-sucedida!");
                    break;
                } else {
                    LOG.warn("Autenticação MFA falhou!");
                }
            }
            scanner.close();
        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }
    }
}
