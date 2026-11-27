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
import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.GoogleIdentityVerifier;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.GoogleLoginNonce;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtProperties;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtService;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CurrentUser currentUser;
    private final UserRepository users;
    private final GoogleLoginNonce googleNonces;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
        return issueSession(((CustomUserDetails) authentication.getPrincipal()).getUser());
    }

    @Transactional
    public LoginResponse loginWithGoogle(GoogleIdentityVerifier.Identity identity, String password) {
        googleNonces.requireActive(identity.nonce());
        var linked = users.findByGoogleSubjectForUpdate(identity.subject());
        if (linked.isPresent()) {
            User user = linked.get();
            requireEnabled(user);
            googleNonces.consume(identity.nonce());
            return issueSession(user);
        }

        var matches = users.findByEmailIgnoringCaseForUpdate(identity.email());
        if (matches.size() > 1) {
            throw new ConflictException("Há mais de uma conta para este e-mail. Entre com sua senha.");
        }

        User user;
        if (matches.isEmpty()) {
            if (!identity.authoritativeEmail()) {
                throw new BusinessException("Cadastre-se na TicketWave com esse e-mail antes de vincular o Google.");
            }
            user = newGoogleUser(identity);
        } else {
            user = matches.getFirst();
            requireEnabled(user);
            if (user.getGoogleSubject() != null && !identity.subject().equals(user.getGoogleSubject())) {
                throw new ConflictException("Esta conta já está vinculada a outro login do Google.");
            }
            if ((!identity.authoritativeEmail() || user.getRole() != Role.ROLE_CUSTOMER)
                    && (password == null || !passwordEncoder.matches(password, user.getPassword()))) {
                throw new GoogleLinkPasswordRequiredException();
            }
        }

        googleNonces.consume(identity.nonce());
        user.setGoogleSubject(identity.subject());
        return issueSession(users.save(user));
    }

    private void requireEnabled(User user) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Usuário indisponível.");
        }
    }

    private User newGoogleUser(GoogleIdentityVerifier.Identity identity) {
        String name = identity.name();
        if (name == null || name.isBlank()) name = identity.email().split("@", 2)[0];
        name = name.strip();
        int length = Math.min(name.codePointCount(0, name.length()), 150);
        var user = new User();
        user.setName(name.substring(0, name.offsetByCodePoints(0, length)));
        user.setEmail(identity.email());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Role.ROLE_CUSTOMER);
        return user;
    }

    private LoginResponse issueSession(User user) {
        String token = jwtService.generateToken(new CustomUserDetails(user));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResponse(token, refreshToken.getToken(), "Bearer", jwtProperties.expiration());
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
        refreshTokenService.revoke(request.refreshToken());
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
