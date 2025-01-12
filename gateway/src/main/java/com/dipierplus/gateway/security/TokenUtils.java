package com.dipierplus.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtils {

    private static final Long ACCESS_TOKEN_VALIDITY_SECONDS = 2_592_000L;
    private static final Key SIGNINGKEY = generateRandomKey();

    private static Key generateRandomKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public static Claims getClaims(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SIGNINGKEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            throw e;
        }
    }

    public static String createAccessToken(UserDetailsImp user) {
        long expirationTime = System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1_000;
        Date expirationDate = new Date(expirationTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("active", user.isEnabled());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("expirationDate", expirationDate);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(expirationDate)
                .addClaims(claims)
                .signWith(SIGNINGKEY)
                .compact();
    }

    public static UsernamePasswordAuthenticationToken getAuthenticationToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is null or empty");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SIGNINGKEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Boolean active = claims.get("active", Boolean.class);

            if (username == null || username.isEmpty()) {
                throw new JwtException("JWT token does not contain a valid subject");
            }

            // Verificar si la cuenta está activa
            if (active == null || !active) {
                throw new JwtException("Account is not active");
            }

            return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());

        } catch (JwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            throw e;
        }
    }
}