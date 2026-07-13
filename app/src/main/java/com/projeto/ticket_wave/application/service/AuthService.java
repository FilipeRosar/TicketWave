package com.projeto.ticket_wave.application.service;


import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginRequest;
import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginResponse;
import com.projeto.ticket_wave.application.dto.UserDTOs.RegisterUserRequest;
import com.projeto.ticket_wave.application.dto.UserDTOs.UserResponse;
import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.domain.enums.Role;
import com.projeto.ticket_wave.infrastructure.exception.ConflitException;
import com.projeto.ticket_wave.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.infrastructure.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtProperties.expiration());
    }
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        return userService.createUser(request);
    }
}
