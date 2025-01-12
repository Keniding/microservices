package com.dipierplus.gateway.security;

import com.dipierplus.gateway.model.AuthRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {
    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationWebFilter.class.getName());
    private final ReactiveAuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationWebFilter(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!request.getURI().getPath().equals("/gt/login") ||
                !request.getMethod().equals(HttpMethod.POST)) {
            return chain.filter(exchange);
        }

        LOGGER.info("Processing authentication filter for request: " + request.getURI());

        return request.getBody()
                .next()
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return objectMapper.readValue(bytes, AuthRequest.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Error reading request body", e);
                    }
                })
                .switchIfEmpty(Mono.error(new AuthenticationCredentialsNotFoundException("No authentication data found")))
                .flatMap(authRequest -> {
                    LOGGER.info("Auth credentials received for user: " + authRequest.getUsername());

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    authRequest.getUsername(),
                                    authRequest.getPassword()
                            );

                    return authenticationManager.authenticate(authToken)
                            .flatMap(authentication -> onAuthenticationSuccess(exchange, authentication))
                            .onErrorResume(e -> onAuthenticationError(exchange, e));
                })
                .onErrorResume(e -> onAuthenticationError(exchange, e));
    }

    private Mono<Void> onAuthenticationSuccess(ServerWebExchange exchange, Authentication authentication) {
        UserDetailsImp userDetails = (UserDetailsImp) authentication.getPrincipal();

        if (!userDetails.isEnabled()) {
            return onAuthenticationError(exchange,
                    new AuthenticationCredentialsNotFoundException("Account is not active"));
        }

        LOGGER.info("Authentication successful for user: " + authentication.getName());

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);

        String token = TokenUtils.createAccessToken(userDetails);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", token);
        responseBody.put("username", userDetails.getUsername());
        responseBody.put("active", userDetails.isEnabled());
        responseBody.put("message", "Authentication successful");

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(responseBody);
            DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> onAuthenticationError(ServerWebExchange exchange, Throwable error) {
        LOGGER.warning("Authentication error: " + error.getMessage());

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "Authentication failed");
        responseBody.put("message", error.getMessage());

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(responseBody);
            DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
