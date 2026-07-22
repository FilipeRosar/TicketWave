package com.projeto.ticket_wave.infrastructure.security;

import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.domain.enums.Role;
import com.projeto.ticket_wave.infrastructure.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AuthenticationFacade {

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new UnauthorizedException("Usuário não autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("Usuário autenticado inválido");
        }

        return customUserDetails.getUser();
    }
    public UUID  getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUser().getRole() ==  Role.ROLE_ADMIN;
    }

}
