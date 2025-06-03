package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.JobPostingResponseDTO;
import com.HyundaiAutoever.ATS.repository.*;
import com.HyundaiAutoever.ATS.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final JobPostingService jobPostingService;
    // Add other repositories as needed

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Calculate dashboard statistics using available repository methods
            stats.put("totalJobs", jobPostingRepository.count());
            stats.put("activeUsers", userRepository.count()); // All users for now
            stats.put("unreadMessages", contactMessageRepository.countByIsReadFalse());
            stats.put("applications", 0L); // Implement when application entity is available
            
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve dashboard stats: " + e.getMessage()));
        }
    }

    @GetMapping("/recent-activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivities() {
        try {
            Map<String, Object> activities = new HashMap<>();
            
            // Add recent activities data using available repository methods
            activities.put("recentMessages", contactMessageRepository.findAllByOrderByCreatedAtDesc());
            
            return ResponseEntity.ok(ApiResponse.success("Recent activities retrieved successfully", activities));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent activities: " + e.getMessage()));
        }
    }

    @GetMapping("/recent-jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobPostingResponseDTO>>> getRecentJobPostings(
            @RequestParam(defaultValue = "2") int limit) {
        try {
            List<JobPostingResponseDTO> recentJobs = jobPostingService.getRecentJobPostings(limit);
            return ResponseEntity.ok(ApiResponse.success("Recent job postings retrieved successfully", recentJobs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent job postings: " + e.getMessage()));
        }
    }
} 