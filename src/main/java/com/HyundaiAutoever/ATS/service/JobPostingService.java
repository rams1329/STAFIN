package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.JobPostingRequestDTO;
import com.HyundaiAutoever.ATS.dto.JobPostingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JobPostingService {
    JobPostingResponseDTO createJobPosting(JobPostingRequestDTO jobPostingDTO, Long userId);
    JobPostingResponseDTO getJobPostingById(Long id, boolean adminView);
    Page<JobPostingResponseDTO> getAllJobPostings(String title, String requirementTitle, Long jobFunctionId, Long jobTypeId, Pageable pageable, boolean adminView);
    
    // Enhanced method with comprehensive filters
    Page<JobPostingResponseDTO> getAllJobPostingsWithAdvancedFilters(
        String search, String title, String requirementTitle, String jobDesignation, String jobDescription,
        Long jobFunctionId, Long jobTypeId, Long locationId, Long departmentId, Long experienceLevelId,
        Boolean isActive, Long createdByUserId,
        LocalDateTime createdDateFrom, LocalDateTime createdDateTo, 
        LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
        BigDecimal salaryMinFrom, BigDecimal salaryMinTo, 
        BigDecimal salaryMaxFrom, BigDecimal salaryMaxTo,
        Pageable pageable, boolean adminView);
    
    JobPostingResponseDTO updateJobPosting(Long id, JobPostingRequestDTO jobPostingDTO);
    void deleteJobPosting(Long id);
    JobPostingResponseDTO copyJobPosting(Long id);
    List<JobPostingResponseDTO> getAllActiveJobPostings();
    JobPostingResponseDTO toggleJobStatus(Long id);
    
    // Method for dashboard - get recent job postings
    List<JobPostingResponseDTO> getRecentJobPostings(int limit);
} 