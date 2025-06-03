package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.UserBasicInfoDTO;
import com.HyundaiAutoever.ATS.dto.UserEducationDTO;
import com.HyundaiAutoever.ATS.dto.UserEmploymentDTO;
import com.HyundaiAutoever.ATS.security.UserPrincipal;
import com.HyundaiAutoever.ATS.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private final UserProfileService userProfileService;

    // Basic Information Endpoints
    @GetMapping("/basic-info")
    public ResponseEntity<ApiResponse<UserBasicInfoDTO>> getUserBasicInfo() {
        try {
            Long userId = getCurrentUserId();
            UserBasicInfoDTO basicInfo = userProfileService.getUserBasicInfo(userId);
            return ResponseEntity.ok(ApiResponse.success("Basic information retrieved successfully", basicInfo));
        } catch (Exception e) {
            log.error("Error retrieving basic information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve basic information: " + e.getMessage()));
        }
    }

    @PostMapping("/basic-info")
    public ResponseEntity<ApiResponse<UserBasicInfoDTO>> saveUserBasicInfo(@Valid @RequestBody UserBasicInfoDTO basicInfoDTO) {
        try {
            Long userId = getCurrentUserId();
            UserBasicInfoDTO saved = userProfileService.saveUserBasicInfo(userId, basicInfoDTO);
            return ResponseEntity.ok(ApiResponse.success("Basic information saved successfully", saved));
        } catch (Exception e) {
            log.error("Error saving basic information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save basic information: " + e.getMessage()));
        }
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<String>> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getCurrentUserId();
            String filePath = userProfileService.uploadResume(userId, file);
            return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", filePath));
        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload resume: " + e.getMessage()));
        }
    }

    // Education Endpoints
    @GetMapping("/education")
    public ResponseEntity<ApiResponse<List<UserEducationDTO>>> getUserEducation() {
        try {
            Long userId = getCurrentUserId();
            List<UserEducationDTO> education = userProfileService.getUserEducation(userId);
            return ResponseEntity.ok(ApiResponse.success("Education information retrieved successfully", education));
        } catch (Exception e) {
            log.error("Error retrieving education information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve education information: " + e.getMessage()));
        }
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<UserEducationDTO>> saveUserEducation(@RequestBody UserEducationDTO educationDTO) {
        try {
            Long userId = getCurrentUserId();
            UserEducationDTO saved = userProfileService.saveUserEducation(userId, educationDTO);
            return ResponseEntity.ok(ApiResponse.success("Education information saved successfully", saved));
        } catch (Exception e) {
            log.error("Error saving education information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save education information: " + e.getMessage()));
        }
    }

    @PutMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<UserEducationDTO>> updateUserEducation(
            @PathVariable Long educationId,
            @RequestBody UserEducationDTO educationDTO) {
        try {
            Long userId = getCurrentUserId();
            UserEducationDTO updated = userProfileService.updateUserEducation(userId, educationId, educationDTO);
            return ResponseEntity.ok(ApiResponse.success("Education information updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating education information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update education information: " + e.getMessage()));
        }
    }

    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserEducation(@PathVariable Long educationId) {
        try {
            Long userId = getCurrentUserId();
            userProfileService.deleteUserEducation(userId, educationId);
            return ResponseEntity.ok(ApiResponse.success("Education information deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting education information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete education information: " + e.getMessage()));
        }
    }

    // Employment Endpoints
    @GetMapping("/employment")
    public ResponseEntity<ApiResponse<List<UserEmploymentDTO>>> getUserEmployment() {
        try {
            Long userId = getCurrentUserId();
            List<UserEmploymentDTO> employment = userProfileService.getUserEmployment(userId);
            return ResponseEntity.ok(ApiResponse.success("Employment information retrieved successfully", employment));
        } catch (Exception e) {
            log.error("Error retrieving employment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve employment information: " + e.getMessage()));
        }
    }

    @PostMapping("/employment")
    public ResponseEntity<ApiResponse<UserEmploymentDTO>> saveUserEmployment(@RequestBody UserEmploymentDTO employmentDTO) {
        try {
            Long userId = getCurrentUserId();
            UserEmploymentDTO saved = userProfileService.saveUserEmployment(userId, employmentDTO);
            return ResponseEntity.ok(ApiResponse.success("Employment information saved successfully", saved));
        } catch (Exception e) {
            log.error("Error saving employment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save employment information: " + e.getMessage()));
        }
    }

    @PutMapping("/employment/{employmentId}")
    public ResponseEntity<ApiResponse<UserEmploymentDTO>> updateUserEmployment(
            @PathVariable Long employmentId,
            @RequestBody UserEmploymentDTO employmentDTO) {
        try {
            Long userId = getCurrentUserId();
            UserEmploymentDTO updated = userProfileService.updateUserEmployment(userId, employmentId, employmentDTO);
            return ResponseEntity.ok(ApiResponse.success("Employment information updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating employment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update employment information: " + e.getMessage()));
        }
    }

    @DeleteMapping("/employment/{employmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserEmployment(@PathVariable Long employmentId) {
        try {
            Long userId = getCurrentUserId();
            userProfileService.deleteUserEmployment(userId, employmentId);
            return ResponseEntity.ok(ApiResponse.success("Employment information deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting employment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete employment information: " + e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        
        throw new RuntimeException("Unable to extract user ID from authentication");
    }
} 