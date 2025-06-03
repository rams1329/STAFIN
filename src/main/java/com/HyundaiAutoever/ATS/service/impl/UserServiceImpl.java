package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.AdminCreateUserRequest;
import com.HyundaiAutoever.ATS.dto.PasswordResetRequest;
import com.HyundaiAutoever.ATS.dto.UpdateProfileRequest;
import com.HyundaiAutoever.ATS.dto.UserRegistrationRequest;
import com.HyundaiAutoever.ATS.dto.OtpVerificationRequest;
import com.HyundaiAutoever.ATS.entity.Role;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.repository.RoleRepository;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import com.HyundaiAutoever.ATS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        if (userRole.isEmpty()) {
            // Create ROLE_USER role if it doesn't exist
            Role newUserRole = new Role();
            newUserRole.setName("ROLE_USER");
            newUserRole.setDescription("Regular user role");
            userRole = Optional.of(roleRepository.save(newUserRole));
        }

        // Encode the password using BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        System.out.println("Registering user: " + request.getEmail());
        System.out.println("Original password: " + request.getPassword());
        System.out.println("Encoded password: " + encodedPassword);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .active(true)
                .firstLogin(false)
                .roles(Set.of(userRole.get()))
                .build();
        
        User savedUser = userRepository.save(user);
        System.out.println("User registered successfully: " + savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public User createUserByAdmin(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        
        Set<Role> roles = request.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId)))
                .collect(Collectors.toSet());
        
        if (roles.isEmpty()) {
            throw new RuntimeException("At least one valid role must be assigned");
        }

        // Generate random password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        
        // Generate OTP for first-time login
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .active(true)
                .firstLogin(true)
                .otp(otp)
                .roles(roles)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Print temporary credentials to console (for testing)
        System.out.println("====================================================");
        System.out.println("CREATED USER - TEMPORARY CREDENTIALS:");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Temporary Password: " + tempPassword);
        System.out.println("OTP: " + otp);
        System.out.println("====================================================");
        
        // Here you would typically send an email with the OTP
        // emailService.sendOtpEmail(user.getEmail(), otp);
        
        return savedUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findUsersByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(roleName)))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User not found with ID: " + user.getId());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserToken(Long userId, String token) {
        User user = findUserById(userId);
        user.setJwtToken(token);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void invalidateToken(Long userId) {
        User user = findUserById(userId);
        user.setJwtToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User resetPassword(Long userId, PasswordResetRequest request) {
        User user = findUserById(userId);
        
        if (user.getPassword() != null && !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        user.setOtp(null);
        
        return userRepository.save(user);
    }

    @Override
    public Map<String, Object> getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        
        return profile;
    }

    @Override
    @Transactional
    public Map<String, Object> updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        
        // Only verify current password if it's provided and we're changing password
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            // If changing password, current password is required
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                throw new RuntimeException("Current password is required when changing password");
            }
            
            // Verify current password
            if (user.getPassword() != null && !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }
        
        // Update name (always allowed)
        user.setName(request.getName());
        
        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        
        // Return updated profile
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", updatedUser.getId());
        profile.put("name", updatedUser.getName());
        profile.put("email", updatedUser.getEmail());
        profile.put("roles", updatedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        
        return profile;
    }

    @Override
    @Transactional
    public User verifyOtpAndSetPassword(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        if (!user.isFirstLogin()) {
            throw new RuntimeException("User has already completed first-time login");
        }
        
        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        
        // Set new password and mark first login as completed
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        user.setOtp(null); // Clear OTP after successful verification
        
        return userRepository.save(user);
    }
} 