package com.dipierplus.gateway.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
                            log.warn("Token inválido");
                            return chain.filter(exchange);
                        }
                    });
        }
        return chain.filter(exchange);
    }

    private Mono<UsernamePasswordAuthenticationToken> validateAndSetContext(String token) {
        return Mono.fromCallable(() -> TokenUtils.getAuthenticationToken(token));
    }
}
