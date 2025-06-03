package com.HyundaiAutoever.ATS.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestUri = request.getRequestURI();
            String method = request.getMethod();
            
            log.debug("Processing request: {} {}", method, requestUri);
            
            // Skip JWT auth for test endpoints
            if (requestUri.startsWith("/api/test")) {
                log.debug("Skipping JWT authentication for test endpoint: {}", requestUri);
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("Found JWT token in request for URI: {}", requestUri);
                
                if (tokenProvider.validateToken(jwt)) {
                    log.debug("JWT token is valid for URI: {}", requestUri);
                    
                    // Use the token provider to get the authentication with proper role handling
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    
                    if (authentication != null) {
                        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                        
                        log.debug("Authenticated user: {} with authorities: {} for URI: {}", 
                                principal.getUsername(),
                                authentication.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toList()),
                                requestUri);
                        
                        // Set the authentication in the security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Log successful authentication for profile endpoints
                        if (requestUri.startsWith("/api/user/profile")) {
                            log.info("✅ Successfully authenticated user {} for profile endpoint: {}", 
                                    principal.getUsername(), requestUri);
                        }
                    } else {
                        log.warn("Failed to extract authentication from valid JWT token for URI: {}", requestUri);
                    }
                } else {
                    log.debug("JWT token validation failed for URI: {}", requestUri);
                    if (requestUri.startsWith("/api/user/profile")) {
                        log.warn("❌ JWT token validation failed for profile endpoint: {}", requestUri);
                    }
                }
            } else {
                log.debug("No JWT token found in request headers for URI: {}", requestUri);
                if (requestUri.startsWith("/api/user/profile")) {
                    log.warn("❌ No JWT token found for profile endpoint: {}", requestUri);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for request: {} {}", 
                     request.getMethod(), request.getRequestURI(), ex);
            
            // Add detailed logging for debugging
            log.error("Request details - Method: {}, URI: {}, Authorization header present: {}", 
                     request.getMethod(), 
                     request.getRequestURI(),
                     request.getHeader("Authorization") != null);
            
            if (request.getHeader("Authorization") != null) {
                String authHeader = request.getHeader("Authorization");
                log.error("Authorization header: {}", authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
            }
            
            // Clear the security context on any authentication error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        log.debug("Authorization header: {}", bearerToken != null ? "Bearer token present" : "No Authorization header");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Extracted JWT token (length: {})", token.length());
            return token;
        }
        
        return null;
    }
} 