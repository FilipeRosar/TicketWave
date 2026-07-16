package com.projeto.ticket_wave.application.service;

import com.projeto.ticket_wave.application.dto.UserDTOs.RegisterUserRequest;
import com.projeto.ticket_wave.application.dto.UserDTOs.UserResponse;
import com.projeto.ticket_wave.application.mapper.UserMapper;
import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.domain.enums.Role;
import com.projeto.ticket_wave.infrastructure.exception.ConflitException;
import com.projeto.ticket_wave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserResponse createUser(RegisterUserRequest dto) {
        if(userRepository.existsByEmail(dto.email())){
            throw new ConflitException("Email já cadastrado");
        }
        User user = new  User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPhone(dto.phone());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        user.setCreatedDate(Instant.now());

        User savedUser = userRepository.save(user);
        return mapper.toDto(savedUser);
    }
    public UserResponse getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Usuário não encontrado"));

        return mapper.toDto(user);

    }
}

