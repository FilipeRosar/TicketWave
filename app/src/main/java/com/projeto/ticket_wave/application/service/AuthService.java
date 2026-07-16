package com.projeto.ticket_wave.application.service;


import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginRequest;
import com.projeto.ticket_wave.application.dto.AuthDTOs.LoginResponse;
import com.projeto.ticket_wave.application.dto.AuthDTOs.RefreshTokenRequest;
import com.projeto.ticket_wave.application.dto.AuthDTOs.RefreshTokenResponse;
import com.projeto.ticket_wave.application.dto.UserDTOs.MeResponse;
import com.projeto.ticket_wave.application.dto.UserDTOs.RegisterUserRequest;
import com.projeto.ticket_wave.application.dto.UserDTOs.UserResponse;
import com.projeto.ticket_wave.domain.entity.RefreshToken;
import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.domain.enums.Role;
import com.projeto.ticket_wave.infrastructure.exception.ConflitException;
import com.projeto.ticket_wave.infrastructure.security.AuthenticationFacade;
import com.projeto.ticket_wave.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.infrastructure.services.JwtService;
import com.projeto.ticket_wave.repository.RefreshTokenRepository;
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
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final AuthenticationFacade authenticationFacade;

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
        User user = authenticationFacade.getCurrnetUser().getUser();
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled()
        );
    }
}
