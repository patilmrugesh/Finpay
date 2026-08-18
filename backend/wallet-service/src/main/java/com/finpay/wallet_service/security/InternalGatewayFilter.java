package com.finpay.wallet_service.security;

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
public class InternalGatewayFilter extends OncePerRequestFilter {

    private static final String GATEWAY_HEADER = "X-Internal-Gateway-Token";

    @Value("${gateway.internal-secret:finpay-internal-secret-key-2026}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader(GATEWAY_HEADER);

        // Reject any direct request that doesn't contain the expected gateway token
        if (token == null || !token.equals(expectedSecret)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "status": 403,
                    "error": "Forbidden",
                    "message": "Direct access to internal microservices is blocked. Please route all requests through the API Gateway (Port 8083)."
                }
            """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}