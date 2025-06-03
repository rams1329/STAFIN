package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.entity.*;
import com.HyundaiAutoever.ATS.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDropdownController {

    private final DepartmentRepository departmentRepository;
    private final JobFunctionRepository jobFunctionRepository;
    private final JobTypeRepository jobTypeRepository;
    private final LocationRepository locationRepository;
    private final ExperienceLevelRepository experienceLevelRepository;

    // Department Admin Endpoints
    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartmentsAdmin() {
        try {
            List<Department> departments = departmentRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> createDepartmentAdmin(@Valid @RequestBody Department department) {
        try {
            if (departmentRepository.existsByName(department.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Department with this name already exists"));
            }
            
            Department savedDepartment = departmentRepository.save(department);
            return ResponseEntity.ok(ApiResponse.success("Department created successfully", savedDepartment));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create department: " + e.getMessage()));
        }
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> updateDepartmentAdmin(
            @PathVariable Long id, 
            @Valid @RequestBody Department department) {
        try {
            return departmentRepository.findById(id)
                    .map(existingDepartment -> {
                        // Check if the new name already exists in another department (excluding current one)
                        if (!existingDepartment.getName().equals(department.getName())) {
                            if (departmentRepository.existsByName(department.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<Department>>body(ApiResponse.error("Department with this name already exists"));
                            }
                        }
                        
                        existingDepartment.setName(department.getName());
                        existingDepartment.setDescription(department.getDescription());
                        existingDepartment.setActive(department.isActive());
                        Department updatedDepartment = departmentRepository.save(existingDepartment);
                        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updatedDepartment));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update department: " + e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteDepartmentAdmin(@PathVariable Long id) {
        try {
            if (departmentRepository.existsById(id)) {
                departmentRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete department: " + e.getMessage()));
        }
    }

    @GetMapping("/departments/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Department>>> getActiveDepartmentsAdmin() {
        try {
            List<Department> departments = departmentRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Active departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active departments: " + e.getMessage()));
        }
    }

    @PutMapping("/departments/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> toggleDepartmentActiveAdmin(@PathVariable Long id) {
        try {
            return departmentRepository.findById(id)
                    .map(existingDepartment -> {
                        existingDepartment.setActive(!existingDepartment.isActive());
                        Department updatedDepartment = departmentRepository.save(existingDepartment);
                        String status = updatedDepartment.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Department " + status + " successfully", updatedDepartment));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle department status: " + e.getMessage()));
        }
    }

    // Job Function Admin Endpoints
    @GetMapping("/job-functions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getAllJobFunctionsAdmin() {
        try {
            List<JobFunction> jobFunctions = jobFunctionRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Job functions retrieved successfully", jobFunctions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve job functions: " + e.getMessage()));
        }
    }

    @PostMapping("/job-functions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> createJobFunctionAdmin(@Valid @RequestBody JobFunction jobFunction) {
        try {
            if (jobFunctionRepository.existsByNameIgnoreCase(jobFunction.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job function with this name already exists"));
            }
            
            jobFunction.setActive(true); // Default to active
            JobFunction savedJobFunction = jobFunctionRepository.save(jobFunction);
            return ResponseEntity.ok(ApiResponse.success("Job function created successfully", savedJobFunction));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create job function: " + e.getMessage()));
        }
    }

    @PutMapping("/job-functions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> updateJobFunctionAdmin(
            @PathVariable Long id, 
            @Valid @RequestBody JobFunction jobFunction) {
        try {
            return jobFunctionRepository.findById(id)
                    .map(existingJobFunction -> {
                        // Check if the new name already exists in another job function (excluding current one)
                        if (!existingJobFunction.getName().equals(jobFunction.getName())) {
                            if (jobFunctionRepository.existsByNameIgnoreCase(jobFunction.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<JobFunction>>body(ApiResponse.error("Job function with this name already exists"));
                            }
                        }
                        
                        existingJobFunction.setName(jobFunction.getName());
                        existingJobFunction.setActive(jobFunction.isActive());
                        JobFunction updatedJobFunction = jobFunctionRepository.save(existingJobFunction);
                        return ResponseEntity.ok(ApiResponse.success("Job function updated successfully", updatedJobFunction));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update job function: " + e.getMessage()));
        }
    }

    @DeleteMapping("/job-functions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteJobFunctionAdmin(@PathVariable Long id) {
        try {
            if (jobFunctionRepository.existsById(id)) {
                jobFunctionRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Job function deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete job function: " + e.getMessage()));
        }
    }

    @GetMapping("/job-functions/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getActiveJobFunctionsAdmin() {
        try {
            List<JobFunction> jobFunctions = jobFunctionRepository.findByActiveTrue();
            return ResponseEntity.ok(ApiResponse.success("Active job functions retrieved successfully", jobFunctions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active job functions: " + e.getMessage()));
        }
    }

    @PutMapping("/job-functions/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobFunction>> toggleJobFunctionActiveAdmin(@PathVariable Long id) {
        try {
            return jobFunctionRepository.findById(id)
                    .map(existingJobFunction -> {
                        existingJobFunction.setActive(!existingJobFunction.isActive());
                        JobFunction updatedJobFunction = jobFunctionRepository.save(existingJobFunction);
                        String status = updatedJobFunction.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Job function " + status + " successfully", updatedJobFunction));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle job function status: " + e.getMessage()));
        }
    }

    // Job Type Admin Endpoints
    @GetMapping("/job-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobType>>> getAllJobTypesAdmin() {
        try {
            List<JobType> jobTypes = jobTypeRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Job types retrieved successfully", jobTypes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve job types: " + e.getMessage()));
        }
    }

    @PostMapping("/job-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobType>> createJobTypeAdmin(@Valid @RequestBody JobType jobType) {
        try {
            if (jobTypeRepository.existsByNameIgnoreCase(jobType.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job type with this name already exists"));
            }
            
            jobType.setActive(true); // Default to active
            JobType savedJobType = jobTypeRepository.save(jobType);
            return ResponseEntity.ok(ApiResponse.success("Job type created successfully", savedJobType));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create job type: " + e.getMessage()));
        }
    }

    @PutMapping("/job-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobType>> updateJobTypeAdmin(
            @PathVariable Long id, 
            @Valid @RequestBody JobType jobType) {
        try {
            return jobTypeRepository.findById(id)
                    .map(existingJobType -> {
                        // Check if the new name already exists in another job type (excluding current one)
                        if (!existingJobType.getName().equals(jobType.getName())) {
                            if (jobTypeRepository.existsByNameIgnoreCase(jobType.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<JobType>>body(ApiResponse.error("Job type with this name already exists"));
                            }
                        }
                        
                        existingJobType.setName(jobType.getName());
                        existingJobType.setActive(jobType.isActive());
                        JobType updatedJobType = jobTypeRepository.save(existingJobType);
                        return ResponseEntity.ok(ApiResponse.success("Job type updated successfully", updatedJobType));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update job type: " + e.getMessage()));
        }
    }

    @DeleteMapping("/job-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteJobTypeAdmin(@PathVariable Long id) {
        try {
            if (jobTypeRepository.existsById(id)) {
                jobTypeRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Job type deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete job type: " + e.getMessage()));
        }
    }

    @GetMapping("/job-types/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobType>>> getActiveJobTypesAdmin() {
        try {
            List<JobType> jobTypes = jobTypeRepository.findByActiveTrue();
            return ResponseEntity.ok(ApiResponse.success("Active job types retrieved successfully", jobTypes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active job types: " + e.getMessage()));
        }
    }

    @PutMapping("/job-types/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobType>> toggleJobTypeActiveAdmin(@PathVariable Long id) {
        try {
            return jobTypeRepository.findById(id)
                    .map(existingJobType -> {
                        existingJobType.setActive(!existingJobType.isActive());
                        JobType updatedJobType = jobTypeRepository.save(existingJobType);
                        String status = updatedJobType.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Job type " + status + " successfully", updatedJobType));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle job type status: " + e.getMessage()));
        }
    }

    // Location Admin Endpoints
    @GetMapping("/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Location>>> getAllLocationsAdmin() {
        try {
            List<Location> locations = locationRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", locations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve locations: " + e.getMessage()));
        }
    }

    @PostMapping("/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Location>> createLocationAdmin(@Valid @RequestBody Location location) {
        try {
            if (locationRepository.existsByName(location.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Location with this name already exists"));
            }
            
            location.setActive(true); // Default to active
            Location savedLocation = locationRepository.save(location);
            return ResponseEntity.ok(ApiResponse.success("Location created successfully", savedLocation));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create location: " + e.getMessage()));
        }
    }

    @PutMapping("/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Location>> updateLocationAdmin(
            @PathVariable Long id, 
            @Valid @RequestBody Location location) {
        try {
            return locationRepository.findById(id)
                    .map(existingLocation -> {
                        // Check if the new name already exists in another location (excluding current one)
                        if (!existingLocation.getName().equals(location.getName())) {
                            if (locationRepository.existsByName(location.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<Location>>body(ApiResponse.error("Location with this name already exists"));
                            }
                        }
                        
                        existingLocation.setName(location.getName());
                        existingLocation.setActive(location.isActive());
                        Location updatedLocation = locationRepository.save(existingLocation);
                        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updatedLocation));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
        }
    }

    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteLocationAdmin(@PathVariable Long id) {
        try {
            if (locationRepository.existsById(id)) {
                locationRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Location deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete location: " + e.getMessage()));
        }
    }

    @GetMapping("/locations/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Location>>> getActiveLocationsAdmin() {
        try {
            List<Location> locations = locationRepository.findByIsActiveTrue();
            return ResponseEntity.ok(ApiResponse.success("Active locations retrieved successfully", locations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active locations: " + e.getMessage()));
        }
    }

    @PutMapping("/locations/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Location>> toggleLocationActiveAdmin(@PathVariable Long id) {
        try {
            return locationRepository.findById(id)
                    .map(existingLocation -> {
                        existingLocation.setActive(!existingLocation.isActive());
                        Location updatedLocation = locationRepository.save(existingLocation);
                        String status = updatedLocation.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Location " + status + " successfully", updatedLocation));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle location status: " + e.getMessage()));
        }
    }

    // Experience Level Admin Endpoints
    @GetMapping("/experience-levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExperienceLevel>>> getAllExperienceLevelsAdmin() {
        try {
            List<ExperienceLevel> experienceLevels = experienceLevelRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Experience levels retrieved successfully", experienceLevels));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve experience levels: " + e.getMessage()));
        }
    }

    @PostMapping("/experience-levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExperienceLevel>> createExperienceLevelAdmin(@Valid @RequestBody ExperienceLevel experienceLevel) {
        try {
            if (experienceLevelRepository.existsByName(experienceLevel.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Experience level with this name already exists"));
            }
            
            experienceLevel.setActive(true); // Default to active
            ExperienceLevel savedExperienceLevel = experienceLevelRepository.save(experienceLevel);
            return ResponseEntity.ok(ApiResponse.success("Experience level created successfully", savedExperienceLevel));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create experience level: " + e.getMessage()));
        }
    }

    @PutMapping("/experience-levels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExperienceLevel>> updateExperienceLevelAdmin(
            @PathVariable Long id, 
            @Valid @RequestBody ExperienceLevel experienceLevel) {
        try {
            return experienceLevelRepository.findById(id)
                    .map(existingExperienceLevel -> {
                        // Check if the new name already exists in another experience level (excluding current one)
                        if (!existingExperienceLevel.getName().equals(experienceLevel.getName())) {
                            if (experienceLevelRepository.existsByName(experienceLevel.getName())) {
                                return ResponseEntity.badRequest()
                                        .<ApiResponse<ExperienceLevel>>body(ApiResponse.error("Experience level with this name already exists"));
                            }
                        }
                        
                        existingExperienceLevel.setName(experienceLevel.getName());
                        existingExperienceLevel.setActive(experienceLevel.isActive());
                        ExperienceLevel updatedExperienceLevel = experienceLevelRepository.save(existingExperienceLevel);
                        return ResponseEntity.ok(ApiResponse.success("Experience level updated successfully", updatedExperienceLevel));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update experience level: " + e.getMessage()));
        }
    }

    @DeleteMapping("/experience-levels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteExperienceLevelAdmin(@PathVariable Long id) {
        try {
            if (experienceLevelRepository.existsById(id)) {
                experienceLevelRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("Experience level deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete experience level: " + e.getMessage()));
        }
    }

    @GetMapping("/experience-levels/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExperienceLevel>>> getActiveExperienceLevelsAdmin() {
        try {
            List<ExperienceLevel> experienceLevels = experienceLevelRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Active experience levels retrieved successfully", experienceLevels));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active experience levels: " + e.getMessage()));
        }
    }

    @PutMapping("/experience-levels/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExperienceLevel>> toggleExperienceLevelActiveAdmin(@PathVariable Long id) {
        try {
            return experienceLevelRepository.findById(id)
                    .map(existingExperienceLevel -> {
                        existingExperienceLevel.setActive(!existingExperienceLevel.isActive());
                        ExperienceLevel updatedExperienceLevel = experienceLevelRepository.save(existingExperienceLevel);
                        String status = updatedExperienceLevel.isActive() ? "activated" : "deactivated";
                        return ResponseEntity.ok(ApiResponse.success("Experience level " + status + " successfully", updatedExperienceLevel));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle experience level status: " + e.getMessage()));
        }
    }
}