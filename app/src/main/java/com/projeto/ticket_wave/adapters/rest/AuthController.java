package com.projeto.ticket_wave.adapters.rest;

import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginRequest;
import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginResponse;
import com.projeto.ticket_wave.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
