package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;

public record EventReportRow(EventStatus status, long events, long ticketsSold) {
}
