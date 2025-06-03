package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.entity.JobFunction;
import com.HyundaiAutoever.ATS.repository.JobFunctionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/job-functions")
@RequiredArgsConstructor
public class JobFunctionController {

    private final JobFunctionRepository jobFunctionRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<JobFunction>>> getAllJobFunctions(
            // Search parameters
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            
            // Filter parameters
            @RequestParam(required = false) Boolean isActive,
            
            // Date range filters
            @RequestParam(required = false) LocalDateTime createdDateFrom,
            @RequestParam(required = false) LocalDateTime createdDateTo,
            @RequestParam(required = false) LocalDateTime modifiedDateFrom,
            @RequestParam(required = false) LocalDateTime modifiedDateTo,
            
            // Pagination and sorting
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        try {
            Page<JobFunction> jobFunctions;
            
            // If no filters are applied, use the simple query
            if (search == null && name == null && description == null && isActive == null &&
                createdDateFrom == null && createdDateTo == null && modifiedDateFrom == null && modifiedDateTo == null) {
                jobFunctions = jobFunctionRepository.findAll(pageable);
            } else {
                // Apply filters using a custom method (to be implemented)
                jobFunctions = jobFunctionRepository.findAllWithFilters(
                    search, name, description, isActive,
                    createdDateFrom, createdDateTo, modifiedDateFrom, modifiedDateTo,
                    pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Job Functions retrieved successfully", jobFunctions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve job functions: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getActiveJobFunctions() {
        try {
            List<JobFunction> jobFunctions = jobFunctionRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Active job functions retrieved successfully", jobFunctions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active job functions: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> createJobFunction(@Valid @RequestBody JobFunction jobFunction) {
        try {
            if (jobFunctionRepository.existsByName(jobFunction.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job Function with this name already exists"));
            }
            
            JobFunction savedJobFunction = jobFunctionRepository.save(jobFunction);
            return ResponseEntity.ok(ApiResponse.success("Job Function created successfully", savedJobFunction));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create job function: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> updateJobFunction(
            @PathVariable Long id, 
            @Valid @RequestBody JobFunction jobFunction) {
        try {
            return jobFunctionRepository.findById(id)
                    .map(existingJobFunction -> {
                        // Check if the new name already exists in another job function (excluding current one)
                        if (!existingJobFunction.getName().equals(jobFunction.getName())) {
                            // Only check for duplicates if the name is being changed
                            if (jobFunctionRepository.existsByName(jobFunction.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<JobFunction>>body(ApiResponse.error("Job Function with this name already exists"));
                            }
                        }
                        
                        existingJobFunction.setName(jobFunction.getName());
                        existingJobFunction.setDescription(jobFunction.getDescription());
                        existingJobFunction.setActive(jobFunction.isActive());
                        JobFunction updatedJobFunction = jobFunctionRepository.save(existingJobFunction);
                        return ResponseEntity.ok(ApiResponse.success("Job Function updated successfully", updatedJobFunction));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update job function: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteJobFunction(@PathVariable Long id) {
        try {
            if (jobFunctionRepository.existsById(id)) {
                jobFunctionRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Job Function deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete job function: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> toggleJobFunctionActive(@PathVariable Long id) {
        try {
            return jobFunctionRepository.findById(id)
                    .map(existingJobFunction -> {
                        existingJobFunction.setActive(!existingJobFunction.isActive());
                        JobFunction updatedJobFunction = jobFunctionRepository.save(existingJobFunction);
                        String status = updatedJobFunction.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Job Function " + status + " successfully", updatedJobFunction));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle job function status: " + e.getMessage()));
        }
    }
} 