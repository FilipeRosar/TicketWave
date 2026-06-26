package com.projeto.ticket_wave.infrastructure.exception;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
