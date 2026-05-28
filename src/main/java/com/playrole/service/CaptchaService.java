package com.playrole.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final String secretKey;
    private final boolean skipVerification;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CaptchaService(
            @Value("${recaptcha.secret}") String secretKey,
            @Value("${recaptcha.skip-verification}") boolean skipVerification) {
        this.secretKey = secretKey;
        this.skipVerification = skipVerification;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void verify(String captchaToken) {
        if (skipVerification) {
            return;
        }

        try {
            String body = "secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(captchaToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERIFY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);

            if (!Boolean.TRUE.equals(result.get("success"))) {
                log.warn("reCAPTCHA verification failed: {}", result.get("error-codes"));
                throw new IllegalArgumentException("CAPTCHA inválido. Por favor, inténtalo de nuevo.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA", e);
            throw new RuntimeException("Error al verificar el CAPTCHA. Inténtalo de nuevo.");
        }
    }
}
