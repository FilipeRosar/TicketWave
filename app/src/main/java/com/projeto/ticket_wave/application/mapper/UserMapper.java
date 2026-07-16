package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.UserDTOs.UserResponse;
import com.projeto.ticket_wave.domain.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {
    public UserResponse toDto(User user) {
        return new UserResponse(
          user.getId(),
          user.getName(),
          user.getEmail(),
          user.getPhone(),
          user.getRole(),
                Instant.now()
        );
    }
}
