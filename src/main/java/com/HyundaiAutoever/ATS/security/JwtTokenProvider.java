package com.HyundaiAutoever.ATS.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Ensure all authorities have ROLE_ prefix for consistency
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::ensureRolePrefix)
                .collect(Collectors.joining(","));
                
        log.debug("Creating JWT token for user: {} with authorities: {}", 
                  userPrincipal.getUsername(), authorities);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("name", userPrincipal.getName())
                .claim("roles", authorities)
                .claim("firstLogin", userPrincipal.isFirstLogin())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String rolesString = claims.get("roles", String.class);
            if (rolesString == null || rolesString.trim().isEmpty()) {
                log.warn("No roles found in JWT token for user: {}", claims.getSubject());
                rolesString = "";
            }

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(rolesString.split(","))
                            .filter(auth -> !auth.trim().isEmpty())
                            .map(String::trim)
                            .map(this::ensureRolePrefix)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            Long userId = claims.get("userId", Long.class);
            String name = claims.get("name", String.class);
            Boolean firstLogin = claims.get("firstLogin", Boolean.class);
            
            log.debug("Extracted authorities from JWT: {} for user: {}", 
                     authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                     claims.getSubject());
            
            // Create UserPrincipal with extracted information
            UserPrincipal principal = UserPrincipal.builder()
                    .id(userId)
                    .name(name)
                    .email(claims.getSubject())
                    .password("") // Password not stored in JWT
                    .firstLogin(firstLogin != null ? firstLogin : false)
                    .enabled(true)
                    .authorities(authorities)
                    .build();

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
            
        } catch (Exception e) {
            log.error("Failed to extract authentication from JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    
    /**
     * Ensures that role names have the ROLE_ prefix required by Spring Security
     * while avoiding double prefixing.
     */
    private String ensureRolePrefix(String authority) {
        if (authority == null || authority.trim().isEmpty()) {
            return authority;
        }
        
        authority = authority.trim();
        
        // Don't add ROLE_ prefix to authorities that already have it or are not roles
        if (authority.startsWith("ROLE_") || authority.startsWith("SCOPE_")) {
            return authority;
        }
        
        // Add ROLE_ prefix to role names
        return "ROLE_" + authority;
    }
} 