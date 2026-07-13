package com.projeto.ticket_wave.infrastructure.services;


import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository  userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + email));
        return new CustomUserDetails(user);
    }

}
