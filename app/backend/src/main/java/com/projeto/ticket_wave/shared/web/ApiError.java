package com.projeto.ticket_wave.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<Violation> violations
) {
    public static ApiError of(HttpStatusCode status, String message, String path) {
        return of(status, message, path, List.of());
    }

    public static ApiError of(HttpStatusCode status, String message, String path, List<Violation> violations) {
        HttpStatus standardStatus = HttpStatus.resolve(status.value());
        String reason = standardStatus == null ? "HTTP error" : standardStatus.getReasonPhrase();
        return new ApiError(Instant.now(), status.value(), reason, message, path, List.copyOf(violations));
    }

    public record Violation(String field, String message) {
    }
}
