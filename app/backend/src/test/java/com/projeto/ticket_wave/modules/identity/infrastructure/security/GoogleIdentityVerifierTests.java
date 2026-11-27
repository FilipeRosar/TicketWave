package com.projeto.ticket_wave.modules.identity.infrastructure.security;

import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleIdentityVerifierTests {
    private static final String CLIENT = "test.apps.googleusercontent.com";
    private final GoogleIdentityVerifier verifier = new GoogleIdentityVerifier(CLIENT);

    @Test
    void acceptsVerifiedGoogleIdentityAndDistinguishesWhoOwnsTheEmail() {
        assertThat(verifier.verifyClaims(token("person@gmail.com").build()).authoritativeEmail()).isTrue();
        assertThat(verifier.verifyClaims(token("person@example.test").build()).authoritativeEmail()).isFalse();
        assertThat(verifier.verifyClaims(token("person@example.test").claim("hd", "example.test").build())
                .authoritativeEmail()).isTrue();
    }

    @Test
    void refusesWrongAudienceIssuerUnverifiedEmailAndExpiredCredential() {
        assertRejected(token("person@gmail.com").audience(List.of("other")));
        assertRejected(token("person@gmail.com").issuer("https://example.test"));
        assertRejected(token("person@gmail.com").claim("email_verified", false));
        assertRejected(token("person@gmail.com").expiresAt(Instant.now().minusSeconds(1)));
        assertRejected(token("person@gmail.com").claim("nonce", "invalid"));
    }

    private void assertRejected(Jwt.Builder token) {
        assertThatThrownBy(() -> verifier.verifyClaims(token.build())).isInstanceOf(UnauthorizedException.class);
    }

    private Jwt.Builder token(String email) {
        return Jwt.withTokenValue("fixture").header("alg", "RS256").issuer("https://accounts.google.com")
                .audience(List.of(CLIENT)).subject("subject").expiresAt(Instant.now().plusSeconds(60))
                .claim("email", email).claim("email_verified", true).claim("nonce", "a".repeat(43));
    }
}
