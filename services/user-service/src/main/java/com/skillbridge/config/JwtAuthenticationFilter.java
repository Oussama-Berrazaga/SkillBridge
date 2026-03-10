package com.skillbridge.config;

import com.skillbridge.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    // First try the forwarded headers from the Gateway
    String userId = request.getHeader("X-User-Id");
    String role = request.getHeader("X-User-Role");
    String email = request.getHeader("X-User-Email");

    if (userId != null && role != null && email != null) {
      // Request came through the Gateway — trust the headers
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          email,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      SecurityContextHolder.getContext().setAuthentication(auth);
      filterChain.doFilter(request, response);
      return;
    }

    // Fallback: direct call with Authorization header (e.g. Postman hitting
    // user-service directly)
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    if (jwtService.isTokenValid(token)) {
      String userEmail = jwtService.extractEmail(token);
      String userRole = jwtService.extractRole(token);

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userEmail,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_" + userRole)));
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }
}