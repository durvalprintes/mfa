package com.poc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mfa {

    public static final Logger LOG = LoggerFactory.getLogger(Mfa.class);
    public static final String APP_NAME = "Veragi";
    public static final String EMAIL_ACCOUNT = "teste@testando.com";
    public static final String QRCODE_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";

    public static void main(String[] args) throws UnsupportedEncodingException {
        LOG.info("POC MFA");

        String secret = Base32.random();

        String qrCodeUrl = generateQrCodeUrl(secret);
        LOG.info("Clique no link e escaneie o QRCode com um Autenticador: {}", qrCodeUrl);

        LOG.info("Insira o código exibido no Autenticador:");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        Totp mfa = new Totp(secret);
        if (mfa.verify(code)) {
            LOG.info("Autenticação MFA bem-sucedida!");
        } else {
            LOG.error("Código inválido. Autenticação MFA falhou!");
        }
    }

    public static String generateQrCodeUrl(String secret) throws UnsupportedEncodingException {
        return QRCODE_PREFIX + URLEncoder.encode(String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s", APP_NAME, EMAIL_ACCOUNT, secret, APP_NAME),
                "UTF-8");
    }
}
