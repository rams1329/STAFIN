package com.HyundaiAutoever.ATS.dto;

import com.HyundaiAutoever.ATS.entity.Department;
import com.HyundaiAutoever.ATS.entity.ExperienceLevel;
import com.HyundaiAutoever.ATS.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingResponseDTO {
    
    private Long id;
    private String requirementTitle;
    private String jobTitle;
    private String jobDesignation;
    private String jobDescription;
    
    private JobFunctionDTO jobFunction;
    private JobTypeDTO jobType;
    private Location location;
    private Department department;
    private ExperienceLevel experienceLevel;
    
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private boolean isActive;
} 