package com.HyundaiAutoever.ATS.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        String origin = request.getHeader("Origin");
        log.debug("CORS Filter - Processing request from origin: {} for URI: {}", origin, request.getRequestURI());
        
        // Allow specific origins for better security
        if (origin != null && (origin.equals("http://localhost:4200") || 
                              origin.equals("http://localhost:3000") || 
                              origin.equals("https://localhost:4200"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // For development, allow all origins
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        
        // Allow credentials (important for JWT authentication)
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Allow all necessary HTTP methods
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD");
        
        // Set max age for preflight requests
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // Allow all necessary headers including Authorization
        response.setHeader("Access-Control-Allow-Headers", 
            "Authorization, Content-Type, X-Requested-With, Accept, Origin, " +
            "Access-Control-Request-Method, Access-Control-Request-Headers, " +
            "X-Auth-Token, Cache-Control, Pragma");
        
        // Expose headers that the client should be able to access
        response.setHeader("Access-Control-Expose-Headers", 
            "Authorization, X-Auth-Token, Access-Control-Allow-Origin, " +
            "Access-Control-Allow-Credentials");
        
        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("CORS Filter - Handling OPTIONS preflight request for: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("CORS Filter initialized");
    }

    @Override
    public void destroy() {
        log.info("CORS Filter destroyed");
    }
} 