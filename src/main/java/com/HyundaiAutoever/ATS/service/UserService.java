package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.AdminCreateUserRequest;
import com.HyundaiAutoever.ATS.dto.PasswordResetRequest;
import com.HyundaiAutoever.ATS.dto.UpdateProfileRequest;
import com.HyundaiAutoever.ATS.dto.UserRegistrationRequest;
import com.HyundaiAutoever.ATS.dto.OtpVerificationRequest;
import com.HyundaiAutoever.ATS.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    User registerUser(UserRegistrationRequest request);
    User createUserByAdmin(AdminCreateUserRequest request);
    Optional<User> findById(Long id);
    User findUserById(Long id);
    User findUserByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findAllUsers();
    List<User> getAllUsers();
    List<User> findUsersByRole(String roleName);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    User updateUser(User user);
    User updateUserToken(Long userId, String token);
    void invalidateToken(Long userId);
    User resetPassword(Long userId, PasswordResetRequest request);
    Map<String, Object> getUserProfile(String email);
    Map<String, Object> updateUserProfile(Long userId, UpdateProfileRequest request);
    User verifyOtpAndSetPassword(OtpVerificationRequest request);
} 