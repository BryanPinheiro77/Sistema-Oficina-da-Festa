package com.oficinadafesta.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DeviceAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Device-Key";

    @Value("${device.masterKey}")
    private String masterKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // só protege /catraca/**
        if (!path.startsWith("/catraca")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(HEADER);

        if (key == null || key.isBlank() || !key.equals(masterKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}