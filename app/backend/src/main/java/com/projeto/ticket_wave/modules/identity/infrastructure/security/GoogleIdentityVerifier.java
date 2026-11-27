package com.projeto.ticket_wave.modules.identity.infrastructure.security;

import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GoogleIdentityVerifier {
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final String clientId;
    private final JwtDecoder decoder;

    public GoogleIdentityVerifier(@Value("${app.google.client-id}") String clientId) {
        this.clientId = clientId.strip();
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        var nimbus = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .restOperations(new RestTemplate(factory)).build();
        nimbus.setClaimSetConverter(MappedJwtClaimSetConverter.withDefaults(Map.of("iss", value -> value)));
        this.decoder = nimbus;
    }

    public String clientId() {
        return clientId;
    }

    public Identity verify(String credential) {
        if (clientId.isBlank()) throw invalidCredential();
        try {
            return verifyClaims(decoder.decode(credential));
        } catch (JwtException | IllegalArgumentException ex) {
            throw invalidCredential();
        }
    }

    Identity verifyClaims(Jwt jwt) {
        String issuer = jwt.getClaimAsString("iss");
        String authorizedParty = jwt.getClaimAsString("azp");
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String nonce = jwt.getClaimAsString("nonce");
        var audience = jwt.getAudience();
        if (issuer == null || !ISSUERS.contains(issuer) || audience == null || !audience.contains(clientId)
                || ((audience.size() > 1 || authorizedParty != null) && !clientId.equals(authorizedParty))
                || jwt.getExpiresAt() == null || !jwt.getExpiresAt().isAfter(Instant.now())
                || !Boolean.TRUE.equals(jwt.getClaims().get("email_verified"))
                || subject == null || subject.isBlank() || subject.length() > 255
                || email == null || email.length() > 255 || !email.matches("[^\\s@]+@[^\\s@]+")
                || nonce == null || !nonce.matches("[A-Za-z0-9_-]{43}")) {
            throw invalidCredential();
        }
        email = email.toLowerCase(Locale.ROOT);
        String hostedDomain = jwt.getClaimAsString("hd");
        boolean authoritativeEmail = email.endsWith("@gmail.com") || (hostedDomain != null && !hostedDomain.isBlank());
        return new Identity(subject, email, jwt.getClaimAsString("name"), nonce, authoritativeEmail);
    }

    private UnauthorizedException invalidCredential() {
        return new UnauthorizedException("Não foi possível verificar a credencial do Google.");
    }

    public record Identity(String subject, String email, String name, String nonce, boolean authoritativeEmail) {
    }
}
