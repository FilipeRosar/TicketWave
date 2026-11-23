package com.projeto.ticket_wave.shared.web;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        Integer status,
        String message,
        T data
) {
}
