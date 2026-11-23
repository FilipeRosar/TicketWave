package com.projeto.ticket_wave.shared.web;

import org.springframework.http.HttpStatusCode;

final class ErrorMessages {
    private ErrorMessages() {
    }

    static String forStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Dados de entrada inválidos.";
            case 401 -> "Usuário não autenticado.";
            case 403 -> "Você não possui permissão para acessar este recurso.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "Formato de resposta solicitado não disponível.";
            case 415 -> "Tipo de conteúdo não suportado.";
            default -> status.is5xxServerError()
                    ? "Ocorreu um erro interno. Tente novamente mais tarde."
                    : "Não foi possível processar a requisição.";
        };
    }
}
