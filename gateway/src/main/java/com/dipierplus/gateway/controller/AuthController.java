package com.dipierplus.gateway.controller;

import com.dipierplus.gateway.model.AuthRequest;
import com.dipierplus.gateway.model.AuthResponse;
import com.dipierplus.gateway.security.TokenUtils;
import com.dipierplus.gateway.security.UserDetailsImp;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.logging.Level;
import java.util.logging.Logger;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private static final Logger LOGGER = Logger.getLogger(AuthRequest.class.getName());

    @PostMapping("/gt/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        LOGGER.log(Level.INFO, "Attempting to authenticate user: " + authRequest.getUsername());

        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()))
                .map(auth -> {
                    LOGGER.log(Level.INFO, "Authentication successful for user: " + auth.getName());
                    String token = TokenUtils.createAccessToken((UserDetailsImp) auth.getPrincipal());
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .doOnError(e -> LOGGER.log(Level.SEVERE, "Authentication error: " + e.getMessage(), e))
                .onErrorResume(AuthenticationException.class, e -> {
                    LOGGER.log(Level.WARNING, "Authentication failed for user: " + authRequest.getUsername());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }
}
