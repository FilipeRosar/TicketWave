package com.projeto.ticket_wave.modules.identity.adapters.rest;

import com.projeto.ticket_wave.modules.identity.application.dto.LoginRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.LoginResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.MeResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.RefreshTokenRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.RefreshTokenResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.RegisterUserRequest;
import com.projeto.ticket_wave.modules.identity.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public void register(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        authService.register(registerUserRequest);
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest);
    }
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(){
        return ResponseEntity.ok(authService.me());
    }
}
