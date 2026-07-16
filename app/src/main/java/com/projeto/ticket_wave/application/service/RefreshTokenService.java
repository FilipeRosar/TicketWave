package com.projeto.ticket_wave.application.service;

import com.projeto.ticket_wave.domain.entity.RefreshToken;
import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.repository.RefreshTokenRepository;
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

        refreshTokenRepository.deleteByUser(user);

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

    public RefreshToken validate(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token inválido"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh Token revogado");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh Token expirado");
        }

        return refreshToken;
    }

    public RefreshToken rotate(String token) {
        RefreshToken current = validate(token);
        current.setRevoked(true);
        refreshTokenRepository.save(current);
        return createRefreshToken(current.getUser());
    }
    public void revoke(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}