package com.dipierplus.gateway.security;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JWTAuthorizationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (
                path.startsWith("/gt/login") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/user")
        ) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            return validateAndSetContext(token)
                    .flatMap(authentication -> {
                        if (authentication != null) {
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                            Mono.just(new SecurityContextImpl(authentication))));
                        } else {
                            log.warn("Token inválido o no se pudo autenticar");
                            return handleUnauthorizedResponse(exchange, true);
                        }
                    }).onErrorResume(JwtException.class, e -> {
                        log.error("Error al validar el token: {}", e.getMessage());
                        return handleUnauthorizedResponse(exchange, true);
                    }).onErrorResume(e -> {
                        log.error("Error inesperado al validar el token", e);
                        return handleUnauthorizedResponse(exchange, false);
                    });
        }

        return handleUnauthorizedResponse(exchange, false);
    }

    private Mono<UsernamePasswordAuthenticationToken> validateAndSetContext(String token) {
        return Mono.fromCallable(() -> TokenUtils.getAuthenticationToken(token))
                .onErrorMap(e -> {
                    if (e instanceof JwtException) {
                        log.error("Token inválido: {}", e.getMessage());
                    } else {
                        log.error("Error inesperado en la validación del token", e);
                    }
                    return e;
                });
    }

    private Mono<Void> handleUnauthorizedResponse(ServerWebExchange exchange, boolean tokenProvided) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseMessage = tokenProvided
                ? "{\"error\": \"No autorizado\", \"message\": \"Token proporcionado, pero es inválido.\"}"
                : "{\"error\": \"No autorizado\", \"message\": \"Token no proporcionado.\"}";

        byte[] bytes = responseMessage.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(bytes)));
    }
}
