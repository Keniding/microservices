package com.dipierplus.gateway.component;

import com.dipierplus.gateway.repository.UserRepository;
import com.dipierplus.gateway.security.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.nio.charset.StandardCharsets;

@Component
public class UserStatusFilter implements WebFilter {

    private final UserRepository userRepository;

    public UserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = TokenUtils.getClaims(token);
                String username = claims.get("username", String.class);

                return userRepository.findByUsername(username)
                        .flatMap(user -> {
                            if (!user.isEnabled()) {
                                return handleErrorResponse(exchange, HttpStatus.FORBIDDEN, "Usuario inactivo");
                            }
                            return chain.filter(exchange);
                        })
                        .switchIfEmpty(handleErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

            } catch (JwtException e) {
                return handleErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token inválido");
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> handleErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        String errorJson = "{\"error\": \"" + message + "\"}";
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(buffer));
    }
}
