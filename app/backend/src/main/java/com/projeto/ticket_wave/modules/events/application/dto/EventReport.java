package com.projeto.ticket_wave.modules.events.application.dto;

import java.util.List;
import java.util.UUID;

public record EventReport(UUID organizationId, List<EventReportRow> byStatus) {
}
