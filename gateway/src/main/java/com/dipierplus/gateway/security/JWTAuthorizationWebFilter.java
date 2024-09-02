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
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(bearerToken -> bearerToken.startsWith(BEARER_PREFIX))
                .map(bearerToken -> bearerToken.substring(BEARER_PREFIX.length()))
                .flatMap(token -> {
                    UsernamePasswordAuthenticationToken authentication = TokenUtils.getAuthenticationToken(token);
                    if (authentication != null) {
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication))));
                    } else {
                        log.warn("Token inválido");
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
