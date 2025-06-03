package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth-debug")
@RequiredArgsConstructor
@Slf4j
public class AuthDebugController {

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("principal", auth.getPrincipal().getClass().getSimpleName());
            response.put("authorities", auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            
            if (auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                response.put("userId", principal.getId());
                response.put("username", principal.getUsername());
                response.put("name", principal.getName());
                response.put("enabled", principal.isEnabled());
                response.put("firstLogin", principal.isFirstLogin());
            }
        } else {
            response.put("authenticated", false);
            response.put("message", "No authentication found");
        }
        
        log.info("Auth debug - Current user info: {}", response);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-authenticated")
    public ResponseEntity<ApiResponse<String>> testAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth debug - Authenticated endpoint accessed by: {}", 
                auth != null ? auth.getName() : "anonymous");
        return ResponseEntity.ok(ApiResponse.success("Authenticated endpoint accessed successfully"));
    }
    
    @GetMapping("/test-admin-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> testAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth debug - Admin role endpoint accessed by: {} with authorities: {}", 
                auth != null ? auth.getName() : "anonymous",
                auth != null ? auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()) : "none");
        return ResponseEntity.ok(ApiResponse.success("Admin role endpoint accessed successfully"));
    }
    
    @GetMapping("/test-role-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> testRoleAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth debug - ROLE_ADMIN endpoint accessed by: {} with authorities: {}", 
                auth != null ? auth.getName() : "anonymous",
                auth != null ? auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()) : "none");
        return ResponseEntity.ok(ApiResponse.success("ROLE_ADMIN endpoint accessed successfully"));
    }
    
    @GetMapping("/test-has-authority")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> testHasAuthority() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth debug - hasAuthority('ROLE_ADMIN') endpoint accessed by: {} with authorities: {}", 
                auth != null ? auth.getName() : "anonymous",
                auth != null ? auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()) : "none");
        return ResponseEntity.ok(ApiResponse.success("hasAuthority('ROLE_ADMIN') endpoint accessed successfully"));
    }
    
    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> getHeaders(@RequestHeader Map<String, String> headers) {
        log.info("Auth debug - Request headers: {}", headers);
        return ResponseEntity.ok(headers);
    }
} 