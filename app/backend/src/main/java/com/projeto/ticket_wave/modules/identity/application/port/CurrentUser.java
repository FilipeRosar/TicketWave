package com.projeto.ticket_wave.modules.identity.application.port;

import com.projeto.ticket_wave.modules.identity.domain.entity.User;

public interface CurrentUser {
    User getCurrentUser();
}
