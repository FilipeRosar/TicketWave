package com.projeto.ticket_wave.modules.identity.infrastructure.security;

import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GoogleLoginNonce {
    private static final String PREFIX = "google-login:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;

    public String issue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(PREFIX + nonce, "1", Duration.ofMinutes(10));
        return nonce;
    }

    public void requireActive(String nonce) {
        if (!Boolean.TRUE.equals(redis.hasKey(PREFIX + nonce))) throw expired();
    }

    public void consume(String nonce) {
        if (redis.opsForValue().getAndDelete(PREFIX + nonce) == null) throw expired();
    }

    private UnauthorizedException expired() {
        return new UnauthorizedException("A tentativa de login com Google expirou. Tente novamente.");
    }
}
