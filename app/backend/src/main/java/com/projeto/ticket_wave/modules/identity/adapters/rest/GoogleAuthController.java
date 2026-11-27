package com.projeto.ticket_wave.modules.identity.adapters.rest;

import com.projeto.ticket_wave.modules.identity.application.dto.GoogleChallenge;
import com.projeto.ticket_wave.modules.identity.application.dto.GoogleLoginRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.LoginResponse;
import com.projeto.ticket_wave.modules.identity.application.service.AuthService;
import com.projeto.ticket_wave.modules.identity.application.service.GoogleLinkPasswordRequiredException;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.GoogleIdentityVerifier;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.GoogleLoginNonce;
import com.projeto.ticket_wave.shared.web.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {
    private final GoogleIdentityVerifier verifier;
    private final GoogleLoginNonce nonces;
    private final AuthService authService;

    @GetMapping("/challenge")
    public ResponseEntity<GoogleChallenge> challenge() {
        String clientId = verifier.clientId();
        if (clientId.isBlank()) return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new GoogleChallenge(clientId, nonces.issue()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody GoogleLoginRequest request) {
        var identity = verifier.verify(request.credential());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(authService.loginWithGoogle(identity, request.password()));
    }

    @ExceptionHandler(GoogleLinkPasswordRequiredException.class)
    ResponseEntity<ApiError> passwordRequired(GoogleLinkPasswordRequiredException error, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).cacheControl(CacheControl.noStore())
                .body(ApiError.of(HttpStatus.PRECONDITION_REQUIRED, error.getMessage(), request.getRequestURI()));
    }
}
