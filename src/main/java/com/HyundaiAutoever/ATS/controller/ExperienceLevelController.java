package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.entity.ExperienceLevel;
import com.HyundaiAutoever.ATS.repository.ExperienceLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/experience-levels")
@RequiredArgsConstructor
public class ExperienceLevelController {

    private final ExperienceLevelRepository experienceLevelRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExperienceLevel>>> getAllExperienceLevels() {
        try {
            List<ExperienceLevel> experienceLevels = experienceLevelRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Experience levels retrieved successfully", experienceLevels));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve experience levels: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExperienceLevel>>> getActiveExperienceLevels() {
        try {
            List<ExperienceLevel> experienceLevels = experienceLevelRepository.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Active experience levels retrieved successfully", experienceLevels));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active experience levels: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExperienceLevel>> createExperienceLevel(@Valid @RequestBody ExperienceLevel experienceLevel) {
        try {
            if (experienceLevelRepository.existsByName(experienceLevel.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Experience level with this name already exists"));
            }
            
            ExperienceLevel savedExperienceLevel = experienceLevelRepository.save(experienceLevel);
            return ResponseEntity.ok(ApiResponse.success("Experience level created successfully", savedExperienceLevel));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create experience level: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExperienceLevel>> updateExperienceLevel(
            @PathVariable Long id, 
            @Valid @RequestBody ExperienceLevel experienceLevel) {
        try {
            return experienceLevelRepository.findById(id)
                    .map(existingLevel -> {
                        existingLevel.setName(experienceLevel.getName());
                        existingLevel.setDescription(experienceLevel.getDescription());
                        existingLevel.setMinYears(experienceLevel.getMinYears());
                        existingLevel.setMaxYears(experienceLevel.getMaxYears());
                        existingLevel.setActive(experienceLevel.isActive());
                        ExperienceLevel updatedLevel = experienceLevelRepository.save(existingLevel);
                        return ResponseEntity.ok(ApiResponse.success("Experience level updated successfully", updatedLevel));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update experience level: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteExperienceLevel(@PathVariable Long id) {
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
}