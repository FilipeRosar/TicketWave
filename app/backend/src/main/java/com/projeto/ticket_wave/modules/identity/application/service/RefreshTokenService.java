package com.projeto.ticket_wave.modules.identity.application.service;

import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.application.port.RefreshTokenRepository;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshToken createRefreshToken(User user) {

        refreshTokenRepository.lockUserSession(user.getId());
        refreshTokenRepository.deleteByUser(user);
        return issue(user);
    }

    private RefreshToken issue(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(
                        Instant.now()
                                .plusMillis(jwtProperties.refreshExpiration())
                )
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken validate(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh Token inválido"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh Token revogado");
        }

        if (!refreshToken.getExpiresAt().isAfter(Instant.now())) {
            throw new UnauthorizedException("Refresh Token expirado");
        }

        if (!Boolean.TRUE.equals(refreshToken.getUser().getEnabled())) {
            throw new UnauthorizedException("Usuário indisponível");
        }

        return refreshToken;
    }

    public RefreshToken rotate(String token) {
        RefreshToken current = lockAndValidate(token);
        refreshTokenRepository.delete(current);
        return issue(current.getUser());
    }

    public void revoke(String token) {
        refreshTokenRepository.delete(lockAndValidate(token));
    }

    private RefreshToken lockAndValidate(String token) {
        RefreshToken current = validate(token);
        refreshTokenRepository.lockUserSession(current.getUser().getId());
        return validate(token);
    }
}
