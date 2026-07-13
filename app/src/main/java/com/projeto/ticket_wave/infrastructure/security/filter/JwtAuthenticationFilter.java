package com.projeto.ticket_wave.infrastructure.security.filter;

import com.projeto.ticket_wave.infrastructure.services.CustomUserDetailsService;
import com.projeto.ticket_wave.infrastructure.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt  = authorization.substring(7);
        String email = jwtService.extractUsername(jwt);

       if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
           UserDetails user = customUserDetailsService.loadUserByUsername(email);

           if(jwtService.isTokenValid(jwt, user.getUsername())) {
               UsernamePasswordAuthenticationToken authentication =
                       new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
               authentication.setDetails(new WebAuthenticationDetailsSource()
                       .buildDetails(request));
               SecurityContextHolder
                       .getContext()
                       .setAuthentication(authentication);

           }
       }
       filterChain.doFilter(request, response);
    }
}
