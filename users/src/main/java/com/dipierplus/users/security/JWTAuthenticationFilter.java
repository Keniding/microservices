package com.dipierplus.users.security;

import com.dipierplus.users.service.UserDetailsImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.*;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws AuthenticationException {
        AuthCredentials authCredentials = new AuthCredentials();

        try {
            authCredentials = new ObjectMapper()
                    .readValue(req.getReader(), AuthCredentials.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An I/O error occurred", e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authCredentials.getUsername(),
                authCredentials.getPassword(),
                Collections.emptyList()
        );

        setDetails(req, authToken);

        return this.getAuthenticationManager().authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication auth
    ) throws IOException, ServletException {
        UserDetailsImp userDetails = (UserDetailsImp) auth.getPrincipal();
        String token = TokenUtils.createAccessToken(userDetails);

        response.addHeader("Authorization", "Bearer " + token);
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, auth);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        LOGGER.log(Level.WARNING, "Authentication failed: " + failed.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Authentication failed: " + failed.getMessage());
        response.getWriter().flush();
    }


}
