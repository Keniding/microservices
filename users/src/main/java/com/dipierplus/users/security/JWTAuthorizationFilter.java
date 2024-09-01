package com.dipierplus.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

            if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
                log.warn("Token ausente o con formato incorrecto");
                filterChain.doFilter(request, response);
                return;
            }

            String token = bearerToken.substring(BEARER_PREFIX.length());

            if (token.isEmpty()) {
                log.warn("Token vacío");
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = TokenUtils.getAuthenticationToken(token);
            if (authentication == null) {
                log.warn("Token inválido");
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Token válido. Usuario autenticado: {}", authentication.getName());
        } catch (IllegalArgumentException e) {
            log.error("Error al procesar el token JWT", e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar el token");
            }
            return;
        }

            if (!response.isCommitted()) {
            filterChain.doFilter(request, response);
        }
    }
}
