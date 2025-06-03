package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.JobTypeDTO;
import com.HyundaiAutoever.ATS.entity.JobType;
import com.HyundaiAutoever.ATS.service.JobTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobTypeController {

    private final JobTypeService jobTypeService;

    // Admin endpoint for all job types
    @GetMapping("/job-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobTypeDTO>> getAllJobTypes() {
        return ResponseEntity.ok(jobTypeService.getAllJobTypes());
    }
    
    // Admin endpoint for active job types - alternative URL
    @GetMapping("/job-types/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobTypeDTO>> getActiveJobTypes() {
        return ResponseEntity.ok(jobTypeService.getAllActiveJobTypes());
    }
    
    // Admin endpoint to get job type by ID
    @GetMapping("/job-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobTypeDTO> getJobTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(jobTypeService.getJobTypeById(id));
    }
    
    // Admin endpoint to create job type
    @PostMapping("/job-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobTypeDTO>> createJobType(@Valid @RequestBody JobTypeDTO jobTypeDTO) {
        try {
            JobTypeDTO createdJobType = jobTypeService.createJobType(jobTypeDTO);
            return ResponseEntity.ok(ApiResponse.success("Job type created successfully", createdJobType));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create job type: " + e.getMessage()));
        }
    }
    
    // Admin endpoint to update job type
    @PutMapping("/job-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobTypeDTO>> updateJobType(
            @PathVariable Long id, 
            @Valid @RequestBody JobTypeDTO jobTypeDTO) {
        try {
            JobTypeDTO updatedJobType = jobTypeService.updateJobType(id, jobTypeDTO);
            return ResponseEntity.ok(ApiResponse.success("Job type updated successfully", updatedJobType));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update job type: " + e.getMessage()));
        }
    }
    
    // Admin endpoint to delete job type
    @DeleteMapping("/job-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteJobType(@PathVariable Long id) {
        try {
            jobTypeService.deleteJobType(id);
            return ResponseEntity.ok(ApiResponse.success("Job type deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete job type: " + e.getMessage()));
        }
    }
}