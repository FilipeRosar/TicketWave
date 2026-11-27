package com.projeto.ticket_wave.modules.identity.application.service;

import com.projeto.ticket_wave.modules.identity.application.dto.ResetPasswordRequest;
import com.projeto.ticket_wave.modules.identity.application.port.PasswordResetRepository;
import com.projeto.ticket_wave.modules.identity.application.port.RefreshTokenRepository;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    static final Duration VALIDITY = Duration.ofMinutes(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final PasswordResetRepository resets;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final PasswordResetMailer mailer;

    @Async("passwordResetExecutor")
    public void request(String email) {
        try {
            var user = users.findByEmail(email).filter(account -> Boolean.TRUE.equals(account.getEnabled()));
            if (user.isEmpty()) return;

            byte[] random = new byte[32];
            RANDOM.nextBytes(random);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            if (resets.issue(user.get().getId(), hash(token), VALIDITY.toSeconds())) {
                mailer.send(user.get().getEmail(), token, VALIDITY);
            }
        } catch (RuntimeException error) {
            log.warn("Falha ao processar recuperação de senha ({})", error.getClass().getSimpleName());
        }
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        var userId = resets.consume(hash(request.token())).orElseThrow(this::invalidToken);
        var user = users.findById(userId).filter(account -> Boolean.TRUE.equals(account.getEnabled()))
                .orElseThrow(this::invalidToken);
        user.setPassword(encoder.encode(request.password()));
        users.save(user);
        refreshTokens.deleteByUser(user);
    }

    private BusinessException invalidToken() {
        return new BusinessException("Link inválido, expirado ou já utilizado. Solicite um novo link.");
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 indisponível", error);
        }
    }
}
