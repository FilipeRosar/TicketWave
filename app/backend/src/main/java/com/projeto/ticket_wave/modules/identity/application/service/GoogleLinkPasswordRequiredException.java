package com.projeto.ticket_wave.modules.identity.application.service;

public class GoogleLinkPasswordRequiredException extends RuntimeException {
    public GoogleLinkPasswordRequiredException() {
        super("Confirme a senha da sua conta TicketWave para vincular o Google.");
    }
}
