package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.ForgotPasswordRequest;
import com.HyundaiAutoever.ATS.dto.JwtResponse;
import com.HyundaiAutoever.ATS.dto.LoginRequest;
import com.HyundaiAutoever.ATS.dto.UserRegistrationRequest;
import com.HyundaiAutoever.ATS.dto.AdminCreateUserRequest;
import com.HyundaiAutoever.ATS.dto.OtpVerificationRequest;
import com.HyundaiAutoever.ATS.security.JwtTokenProvider;
import com.HyundaiAutoever.ATS.security.UserPrincipal;
import com.HyundaiAutoever.ATS.service.UserService;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.util.EmailService;
import com.HyundaiAutoever.ATS.util.OtpGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Attempting login for user: " + loginRequest.getEmail());
            
            // Check if user exists before attempting authentication
            if (!userService.existsByEmail(loginRequest.getEmail())) {
                System.out.println("Login failed: User not found with email: " + loginRequest.getEmail());
                return ResponseEntity.status(401).body(ApiResponse.error("Authentication failed: User not found"));
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            System.out.println("Login successful for user: " + userPrincipal.getUsername());
            
            // Check if user has admin role
            boolean isAdmin = userPrincipal.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            String userRole = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
            
            JwtResponse responseData = JwtResponse.builder()
                    .token(jwt)
                    .id(userPrincipal.getId())
                    .name(userPrincipal.getName())
                    .email(userPrincipal.getUsername())
                    .role(userRole)
                    .isAdmin(isAdmin)
                    .firstLogin(userPrincipal.isFirstLogin())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", responseData));
        } catch (Exception e) {
            System.out.println("Login failed with exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Attempting admin login for user: " + loginRequest.getEmail());
            
            // Check if user exists before attempting authentication
            if (!userService.existsByEmail(loginRequest.getEmail())) {
                System.out.println("Admin login failed: User not found with email: " + loginRequest.getEmail());
                return ResponseEntity.status(401).body(ApiResponse.error("Authentication failed: User not found"));
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Check if user has admin role
            boolean isAdmin = userPrincipal.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                System.out.println("Admin login failed: User does not have admin role: " + loginRequest.getEmail());
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied: Admin privileges required"));
            }
            
            String jwt = tokenProvider.createToken(authentication);
            System.out.println("Admin login successful for user: " + userPrincipal.getUsername());
            
            JwtResponse responseData = JwtResponse.builder()
                    .token(jwt)
                    .id(userPrincipal.getId())
                    .name(userPrincipal.getName())
                    .email(userPrincipal.getUsername())
                    .role("ROLE_ADMIN")
                    .isAdmin(true)
                    .firstLogin(userPrincipal.isFirstLogin())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("Admin login successful", responseData));
        } catch (Exception e) {
            System.out.println("Admin login failed with exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createUserByAdmin(@Valid @RequestBody AdminCreateUserRequest request) {
        try {
            User user = userService.createUserByAdmin(request);
            return ResponseEntity.ok(ApiResponse.success("User created successfully. OTP sent to email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Processing forgot password request for email: {}", request.getEmail());
            
            // Find user by email
            User user = userService.findUserByEmail(request.getEmail());
            
            // Generate a 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            
            // Update user with OTP and set firstLogin to true
            user.setOtp(otp);
            user.setFirstLogin(true);
            userService.updateUser(user);
            
            // Log the OTP for testing purposes
            log.info("======================================");
            log.info("FORGOT PASSWORD - OTP GENERATED:");
            log.info("Email: {}", user.getEmail());
            log.info("OTP: {}", otp);
            log.info("======================================");
            
            // In production, you would send an email with the OTP
            try {
                // For development purposes, we're just logging the OTP
                // emailService.sendOtpEmail(user.getEmail(), otp, user.getName());
                log.info("OTP would be sent via email in production");
            } catch (Exception e) {
                log.error("Failed to send OTP email: {}", e.getMessage());
                // Continue with the process even if email fails
            }
            
            return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to your email"));
        } catch (RuntimeException e) {
            log.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            log.info("Processing OTP verification for email: {}", request.getEmail());
            userService.verifyOtpAndSetPassword(request);
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully. You can now login with your new password."));
        } catch (RuntimeException e) {
            log.error("OTP verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 