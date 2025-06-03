package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.entity.Department;
import com.HyundaiAutoever.ATS.repository.DepartmentRepository;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Department>>> getAllDepartments(
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
            Page<Department> departments;
            
            // If no filters are applied, use the simple query
            if (search == null && name == null && description == null && isActive == null &&
                createdDateFrom == null && createdDateTo == null && modifiedDateFrom == null && modifiedDateTo == null) {
                departments = departmentRepository.findAll(pageable);
            } else {
                // Apply filters using a custom method (to be implemented)
                departments = departmentRepository.findAllWithFilters(
                    search, name, description, isActive,
                    createdDateFrom, createdDateTo, modifiedDateFrom, modifiedDateTo,
                    pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Department>>> getActiveDepartments() {
        try {
            List<Department> departments = departmentRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Active departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active departments: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody Department department) {
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id, 
            @Valid @RequestBody Department department) {
        try {
            return departmentRepository.findById(id)
                    .map(existingDepartment -> {
                        // Check if the new name already exists in another department (excluding current one)
                        if (!existingDepartment.getName().equals(department.getName())) {
                            // Only check for duplicates if the name is being changed
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteDepartment(@PathVariable Long id) {
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
    
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> toggleDepartmentActive(@PathVariable Long id) {
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
}