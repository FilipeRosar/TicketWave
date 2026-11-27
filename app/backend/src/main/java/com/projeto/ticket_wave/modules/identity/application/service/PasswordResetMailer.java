package com.projeto.ticket_wave.modules.identity.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Component
@RequiredArgsConstructor
class PasswordResetMailer {
    private final JavaMailSender sender;

    @Value("${app.password-reset.frontend-url}")
    private String frontendUrl;

    @Value("${app.password-reset.from}")
    private String from;

    void send(String email, String token, Duration validity) {
        String link = UriComponentsBuilder.fromUriString(frontendUrl)
                .pathSegment("redefinir-senha").fragment(token).build().toUriString();
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Redefina sua senha na TicketWave");
        message.setText("""
                Recebemos um pedido para redefinir sua senha na TicketWave.

                Acesse o link abaixo em até %d minutos para escolher uma nova senha:
                %s

                Se você não solicitou a troca, ignore este e-mail.
                """.formatted(validity.toMinutes(), link));
        sender.send(message);
    }
}
