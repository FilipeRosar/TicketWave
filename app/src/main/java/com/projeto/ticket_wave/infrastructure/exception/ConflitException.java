package com.projeto.ticket_wave.infrastructure.exception;

public class ConflitException extends RuntimeException {
    public ConflitException(String message) {
        super(message);
    }
}
