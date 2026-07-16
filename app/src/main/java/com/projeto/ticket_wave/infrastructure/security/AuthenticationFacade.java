package com.projeto.ticket_wave.infrastructure.security;

import com.projeto.ticket_wave.infrastructure.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {
    public CustomUserDetails getCurrnetUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }
}
