package com.projeto.ticket_wave.shared.web;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
