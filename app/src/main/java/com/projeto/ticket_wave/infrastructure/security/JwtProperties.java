package com.projeto.ticket_wave.infrastructure.security;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Long expiration,
        Long refreshExpiration
) {

}
