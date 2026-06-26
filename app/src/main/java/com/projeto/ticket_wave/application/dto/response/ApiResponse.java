package com.projeto.ticket_wave.application.dto.response;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        Integer status,
        String message,
        T data
) {
}
