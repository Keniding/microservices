package com.dipierplus.users.security;

import com.dipierplus.users.service.UserDetailsImp;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.security.Key;

@Component
public class TokenUtils {

    private static final Long ACCESS_TOKEN_VALIDITY_SECONDS = 2_592_000L;
    private static final Key SIGNINGKEY = generateRandomKey();

    private static Key generateRandomKey() {
        return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    }

    public static String createAccessToken(UserDetailsImp user) {
        long expirationTime = System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1_000;
        Date expirationDate = new Date(expirationTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("user", user);
        claims.put("expirationDate", expirationDate);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(expirationDate)
                .addClaims(claims)
                .signWith(SIGNINGKEY)
                .compact();
    }

    public static UsernamePasswordAuthenticationToken getAuthenticationToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is null or empty");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SIGNINGKEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();

            return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        } catch (JwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Token is null or empty: " + e.getMessage());
            return null;
        }
    }
}