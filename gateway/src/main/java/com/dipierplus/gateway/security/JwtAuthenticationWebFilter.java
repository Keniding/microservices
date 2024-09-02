package com.dipierplus.gateway.security;

import com.dipierplus.gateway.model.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationWebFilter.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ReactiveAuthenticationManager authenticationManager;

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        LOGGER.log(Level.INFO, "Processing authentication filter for request: " + request.getURI());

        return request.getBody()
                .next()
                .flatMap(dataBuffer -> {
                    try {
                        AuthRequest authCredentials = objectMapper.readValue(dataBuffer.asInputStream(), AuthRequest.class);
                        LOGGER.log(Level.INFO, "Auth credentials received: " + authCredentials.getUsername());

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                authCredentials.getUsername(),
                                authCredentials.getPassword(),
                                Collections.emptyList()
                        );

                        DataBufferUtils.release(dataBuffer);

                        return authenticate(authToken)
                                .flatMap(auth -> {
                                    SecurityContext context = new SecurityContextImpl(auth);
                                    LOGGER.log(Level.INFO, "Authentication successful for user: " + auth.getName());
                                    return successfulAuthentication(exchange, auth)
                                            .then(chain.filter(exchange)
                                                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))));
                                })
                                .switchIfEmpty(unsuccessfulAuthentication(response, "Authentication failed"));
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "An I/O error occurred while reading the request body", e);
                        DataBufferUtils.release(dataBuffer);
                        return unsuccessfulAuthentication(response, "Invalid request");
                    }
                })
                .switchIfEmpty(unsuccessfulAuthentication(response, "No authentication data found"))
                .doOnTerminate(() -> LOGGER.log(Level.INFO, "Finished processing authentication filter"))
                .then();
    }


    private Mono<Authentication> authenticate(UsernamePasswordAuthenticationToken authToken) {
        return authenticationManager.authenticate(authToken)
                .doOnSuccess(auth -> {
                    if (auth.isAuthenticated()) {
                        LOGGER.log(Level.INFO, "Authentication successful for user: " + auth.getName());
                    }
                })
                .doOnError(e -> {
                    LOGGER.log(Level.WARNING, "Authentication failed: " + e.getMessage());
                });
    }

    private Mono<Void> successfulAuthentication(ServerWebExchange exchange, Authentication auth) {
        ServerHttpResponse response = exchange.getResponse();
        UserDetailsImp userDetails = (UserDetailsImp) auth.getPrincipal();
        String token = TokenUtils.createAccessToken(userDetails);

        response.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return Mono.empty();
    }

    private Mono<Void> unsuccessfulAuthentication(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(message.getBytes())));
    }
}
