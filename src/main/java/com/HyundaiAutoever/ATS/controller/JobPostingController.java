package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.JobPostingRequestDTO;
import com.HyundaiAutoever.ATS.dto.JobPostingResponseDTO;
import com.HyundaiAutoever.ATS.security.UserPrincipal;
import com.HyundaiAutoever.ATS.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class JobPostingController {

    private final JobPostingService jobPostingService;

    // Admin endpoints
    @PostMapping("/admin/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> createJobPosting(
            @Valid @RequestBody JobPostingRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("🔧 createJobPosting called with user: {}, requestDTO: {}", 
                currentUser != null ? currentUser.getUsername() : "null", requestDTO);
        
        return new ResponseEntity<>(
                jobPostingService.createJobPosting(requestDTO, currentUser.getId()), 
                HttpStatus.CREATED);
    }

    // Alternative endpoint for job creation - supports frontend calling /api/jobs
    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> createJobPostingAlternative(
            @Valid @RequestBody JobPostingRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("🔧 createJobPostingAlternative called with user: {}, requestDTO: {}", 
                currentUser != null ? currentUser.getUsername() : "null", requestDTO);
        
        return new ResponseEntity<>(
                jobPostingService.createJobPosting(requestDTO, currentUser.getId()), 
                HttpStatus.CREATED);
    }

    @GetMapping("/admin/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<JobPostingResponseDTO>> getAllJobPostingsAdmin(
            // Search parameters
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String requirementTitle,
            @RequestParam(required = false) String jobDesignation,
            @RequestParam(required = false) String jobDescription,
            
            // Filter parameters
            @RequestParam(required = false) Long jobFunctionId,
            @RequestParam(required = false) Long jobTypeId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long experienceLevelId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long createdByUserId,
            
            // Date range filters
            @RequestParam(required = false) LocalDateTime createdDateFrom,
            @RequestParam(required = false) LocalDateTime createdDateTo,
            @RequestParam(required = false) LocalDateTime modifiedDateFrom,
            @RequestParam(required = false) LocalDateTime modifiedDateTo,
            
            // Salary range filters
            @RequestParam(required = false) BigDecimal salaryMinFrom,
            @RequestParam(required = false) BigDecimal salaryMinTo,
            @RequestParam(required = false) BigDecimal salaryMaxFrom,
            @RequestParam(required = false) BigDecimal salaryMaxTo,
            
            // Pagination and sorting
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(
                jobPostingService.getAllJobPostingsWithAdvancedFilters(
                    search, title, requirementTitle, jobDesignation, jobDescription,
                    jobFunctionId, jobTypeId, locationId, departmentId, experienceLevelId,
                    isActive, createdByUserId,
                    createdDateFrom, createdDateTo, modifiedDateFrom, modifiedDateTo,
                    salaryMinFrom, salaryMinTo, salaryMaxFrom, salaryMaxTo,
                    pageable, true));
    }

    @GetMapping("/admin/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> getJobPostingByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getJobPostingById(id, true));
    }

    @PutMapping("/admin/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> updateJobPosting(
            @PathVariable Long id, 
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        return ResponseEntity.ok(jobPostingService.updateJobPosting(id, requestDTO));
    }

    // Alternative endpoint for job updates - supports frontend calling /api/jobs
    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> updateJobPostingAlternative(
            @PathVariable Long id, 
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        return ResponseEntity.ok(jobPostingService.updateJobPosting(id, requestDTO));
    }

    @DeleteMapping("/admin/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobPosting(@PathVariable Long id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.noContent().build();
    }

    // Alternative endpoint for job deletion - supports frontend calling /api/jobs
    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobPostingAlternative(@PathVariable Long id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/jobs/copy/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> copyJobPosting(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.copyJobPosting(id));
    }

    // Alternative endpoint for job toggle status - supports frontend calling /api/jobs
    @PatchMapping("/jobs/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponseDTO> toggleJobStatusAlternative(@PathVariable Long id) {
        JobPostingResponseDTO job = jobPostingService.toggleJobStatus(id);
        return ResponseEntity.ok(job);
    }

    // User endpoints (accessible to both users and admins)
    @GetMapping("/jobs")
    public ResponseEntity<Page<JobPostingResponseDTO>> getAllActiveJobPostings(
            // Search parameters
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String requirementTitle,
            @RequestParam(required = false) String jobDesignation,
            
            // Filter parameters
            @RequestParam(required = false) Long jobFunctionId,
            @RequestParam(required = false) Long jobTypeId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long experienceLevelId,
            
            // Salary range filters
            @RequestParam(required = false) BigDecimal salaryMinFrom,
            @RequestParam(required = false) BigDecimal salaryMinTo,
            @RequestParam(required = false) BigDecimal salaryMaxFrom,
            @RequestParam(required = false) BigDecimal salaryMaxTo,
            
            // Pagination and sorting
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(
                jobPostingService.getAllJobPostingsWithAdvancedFilters(
                    search, title, requirementTitle, jobDesignation, null,
                    jobFunctionId, jobTypeId, locationId, departmentId, experienceLevelId,
                    true, null, // Only active jobs for public view
                    null, null, null, null, // No date filters for public
                    salaryMinFrom, salaryMinTo, salaryMaxFrom, salaryMaxTo,
                    pageable, false));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobPostingResponseDTO> getActiveJobPostingById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getJobPostingById(id, false));
    }
    
    // Public endpoints (no authentication required)
    @GetMapping("/public/jobs")
    public ResponseEntity<ApiResponse<List<JobPostingResponseDTO>>> getPublicJobs() {
        List<JobPostingResponseDTO> jobs = jobPostingService.getAllActiveJobPostings();
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", jobs));
    }
    
    @GetMapping("/public/jobs/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponseDTO>> getPublicJobById(@PathVariable Long id) {
        JobPostingResponseDTO job = jobPostingService.getJobPostingById(id, false);
        return ResponseEntity.ok(ApiResponse.success("Job details retrieved successfully", job));
    }

    // TEST ENDPOINT - Remove after debugging
    @PostMapping("/jobs/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testJobCreation() {
        log.info("🔧 testJobCreation called - controller is reachable!");
        return ResponseEntity.ok("Job creation controller is working!");
    }

    // TEST ENDPOINT - Create sample jobs for debugging
    @PostMapping("/admin/jobs/create-samples")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createSampleJobs(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("🔧 Creating sample jobs for debugging...");
        
        try {
            // Create a few sample job postings
            for (int i = 1; i <= 3; i++) {
                JobPostingRequestDTO sampleJob = new JobPostingRequestDTO();
                sampleJob.setRequirementTitle("REQ-2024-00" + i);
                sampleJob.setJobTitle("Sample Job " + i);
                sampleJob.setJobDesignation("Software Engineer " + i);
                sampleJob.setJobDescription("This is a sample job description for testing purposes. Job " + i + " details...");
                sampleJob.setJobFunctionId(1L); // Assuming ID 1 exists
                sampleJob.setJobTypeId(1L);     // Assuming ID 1 exists  
                sampleJob.setLocationId(1L);    // Assuming ID 1 exists
                
                // Properly convert double to BigDecimal - salary should be set by admin during job creation
                // These are just sample values for testing, in production admin sets these values
                sampleJob.setSalaryMin(BigDecimal.valueOf(50000.0));
                sampleJob.setSalaryMax(BigDecimal.valueOf(80000.0));
                sampleJob.setActive(true);
                
                jobPostingService.createJobPosting(sampleJob, currentUser.getId());
            }
            
            return ResponseEntity.ok("Sample jobs created successfully!");
        } catch (Exception e) {
            log.error("Error creating sample jobs: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating sample jobs: " + e.getMessage());
        }
    }
} 