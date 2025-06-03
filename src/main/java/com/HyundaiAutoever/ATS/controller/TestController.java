package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final Environment environment;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/users")
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success("All users", userRepository.findAll());
    }
    
    @GetMapping("/db-info")
    public ApiResponse<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("activeProfiles", Arrays.toString(environment.getActiveProfiles()));
        
        try {
            dbInfo.put("databaseUrl", dataSource.getConnection().getMetaData().getURL());
            dbInfo.put("databaseProduct", dataSource.getConnection().getMetaData().getDatabaseProductName());
            dbInfo.put("databaseVersion", dataSource.getConnection().getMetaData().getDatabaseProductVersion());
            
            // Count users
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            dbInfo.put("userCount", userCount);
            
            // Check if roles table exists and count roles
            try {
                Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
                dbInfo.put("roleCount", roleCount);
            } catch (Exception e) {
                dbInfo.put("roleCount", "Table not found or error: " + e.getMessage());
            }
            
            return ApiResponse.success("Database information", dbInfo);
        } catch (Exception e) {
            dbInfo.put("error", e.getMessage());
            return ApiResponse.error("Error getting database information: " + e.getMessage());
        }
    }

    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new HashMap<>();
        
        info.put("authenticated", auth.isAuthenticated());
        info.put("principal", auth.getPrincipal().toString());
        info.put("name", auth.getName());
        info.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        info.put("details", auth.getDetails() != null ? auth.getDetails().toString() : null);
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/user-access")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> userAccess() {
        return ResponseEntity.ok("User role access successful");
    }
    
    @GetMapping("/admin-access")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminAccess() {
        return ResponseEntity.ok("Admin role access successful");
    }
} 