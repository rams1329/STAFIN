package com.HyundaiAutoever.ATS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingRequestDTO {
    
    private Long id;
    
    @NotBlank(message = "Requirement title is required")
    private String requirementTitle;
    
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    @NotBlank(message = "Job designation is required")
    private String jobDesignation;
    
    @NotBlank(message = "Job description is required")
    private String jobDescription;
    
    @NotNull(message = "Job function is required")
    private Long jobFunctionId;
    
    @NotNull(message = "Job type is required")
    private Long jobTypeId;
    
    @NotNull(message = "Location is required")
    private Long locationId;
    
    private Long departmentId;
    
    private Long experienceLevelId;
    
    private BigDecimal salaryMin;
    
    private BigDecimal salaryMax;
    
    @Builder.Default
    private boolean isActive = true;
} 