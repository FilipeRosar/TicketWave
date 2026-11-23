package com.projeto.ticket_wave.modules.identity.application.service;


import com.projeto.ticket_wave.modules.identity.application.dto.LoginRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.LoginResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.MeResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.RefreshTokenRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.RefreshTokenResponse;
import com.projeto.ticket_wave.modules.identity.application.dto.RegisterUserRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.UserResponse;
import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.modules.identity.application.port.RefreshTokenRepository;
import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final CurrentUser currentUser;

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
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.rotate(refreshTokenRequest.refreshToken());
        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);

        return new RefreshTokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtProperties.expiration()
        );

    }
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.refreshToken());
        refreshTokenRepository.delete(refreshToken);
    }
    public MeResponse me(){
        User user = currentUser.getCurrentUser();
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled()
        );
    }
}
