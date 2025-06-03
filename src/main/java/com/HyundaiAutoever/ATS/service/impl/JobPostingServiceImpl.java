package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.JobFunctionDTO;
import com.HyundaiAutoever.ATS.dto.JobPostingRequestDTO;
import com.HyundaiAutoever.ATS.dto.JobPostingResponseDTO;
import com.HyundaiAutoever.ATS.dto.JobTypeDTO;
import com.HyundaiAutoever.ATS.entity.JobFunction;
import com.HyundaiAutoever.ATS.entity.JobPosting;
import com.HyundaiAutoever.ATS.entity.JobType;
import com.HyundaiAutoever.ATS.entity.Location;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.entity.Department;
import com.HyundaiAutoever.ATS.entity.ExperienceLevel;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.JobFunctionRepository;
import com.HyundaiAutoever.ATS.repository.JobPostingRepository;
import com.HyundaiAutoever.ATS.repository.JobTypeRepository;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import com.HyundaiAutoever.ATS.repository.LocationRepository;
import com.HyundaiAutoever.ATS.repository.DepartmentRepository;
import com.HyundaiAutoever.ATS.repository.ExperienceLevelRepository;
import com.HyundaiAutoever.ATS.service.JobPostingService;
import com.HyundaiAutoever.ATS.util.JobPostingPredicate;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobFunctionRepository jobFunctionRepository;
    private final JobTypeRepository jobTypeRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final ExperienceLevelRepository experienceLevelRepository;

    @Override
    @Transactional
    public JobPostingResponseDTO createJobPosting(JobPostingRequestDTO requestDTO, Long userId) {
        // Get references to required entities
        JobFunction jobFunction = jobFunctionRepository.findById(requestDTO.getJobFunctionId())
                .orElseThrow(() -> new ResourceNotFoundException("Job function not found with id: " + requestDTO.getJobFunctionId()));
        
        JobType jobType = jobTypeRepository.findById(requestDTO.getJobTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Job type not found with id: " + requestDTO.getJobTypeId()));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Get location (required)
        Location location = locationRepository.findById(requestDTO.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + requestDTO.getLocationId()));
        
        // Get optional entities
        Department department = null;
        if (requestDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(requestDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + requestDTO.getDepartmentId()));
        }
        
        ExperienceLevel experienceLevel = null;
        if (requestDTO.getExperienceLevelId() != null) {
            experienceLevel = experienceLevelRepository.findById(requestDTO.getExperienceLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Experience level not found with id: " + requestDTO.getExperienceLevelId()));
        }
        
        // Create and save job posting
        JobPosting jobPosting = JobPosting.builder()
                .requirementTitle(requestDTO.getRequirementTitle())
                .jobTitle(requestDTO.getJobTitle())
                .jobDesignation(requestDTO.getJobDesignation())
                .jobDescription(requestDTO.getJobDescription())
                .jobFunction(jobFunction)
                .jobType(jobType)
                .location(location)
                .department(department)
                .experienceLevel(experienceLevel)
                .salaryMin(requestDTO.getSalaryMin())
                .salaryMax(requestDTO.getSalaryMax())
                .createdByUser(user)
                .createdDate(LocalDateTime.now())
                .isActive(requestDTO.isActive())
                .build();
        
        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);
        return mapToResponseDTO(savedJobPosting);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponseDTO getJobPostingById(Long id, boolean adminView) {
        JobPosting jobPosting;
        
        if (adminView) {
            jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + id));
        } else {
            jobPosting = jobPostingRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active job posting not found with id: " + id));
        }
        
        return mapToResponseDTO(jobPosting);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobPostingResponseDTO> getAllJobPostings(String title, String requirementTitle, 
                                                      Long jobFunctionId, Long jobTypeId, 
                                                      Pageable pageable, boolean adminView) {
        BooleanBuilder predicate = new BooleanBuilder();
        
        // Add filters
        if (title != null) {
            predicate.and(JobPostingPredicate.hasTitle(title));
        }
        
        if (requirementTitle != null) {
            predicate.and(JobPostingPredicate.hasRequirementTitle(requirementTitle));
        }
        
        if (jobFunctionId != null) {
            predicate.and(JobPostingPredicate.hasJobFunction(jobFunctionId));
        }
        
        if (jobTypeId != null) {
            predicate.and(JobPostingPredicate.hasJobType(jobTypeId));
        }
        
        // If not admin view, show only active jobs
        if (!adminView) {
            predicate.and(JobPostingPredicate.isActive());
        }
        
        return jobPostingRepository.findAll(predicate, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponseDTO> getAllActiveJobPostings() {
        // Create a predicate for active jobs only
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(JobPostingPredicate.isActive());
        
        // Get all active job postings without pagination
        Iterable<JobPosting> jobPostings = jobPostingRepository.findAll(predicate);
        
        // Convert to list of DTOs
        return StreamSupport.stream(jobPostings.spliterator(), false)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobPostingResponseDTO updateJobPosting(Long id, JobPostingRequestDTO requestDTO) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + id));
        
        JobFunction jobFunction = jobFunctionRepository.findById(requestDTO.getJobFunctionId())
                .orElseThrow(() -> new ResourceNotFoundException("Job function not found with id: " + requestDTO.getJobFunctionId()));
        
        JobType jobType = jobTypeRepository.findById(requestDTO.getJobTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Job type not found with id: " + requestDTO.getJobTypeId()));
        
        // Get location (required)
        Location location = locationRepository.findById(requestDTO.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + requestDTO.getLocationId()));
        
        // Get optional entities
        Department department = null;
        if (requestDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(requestDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + requestDTO.getDepartmentId()));
        }
        
        ExperienceLevel experienceLevel = null;
        if (requestDTO.getExperienceLevelId() != null) {
            experienceLevel = experienceLevelRepository.findById(requestDTO.getExperienceLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Experience level not found with id: " + requestDTO.getExperienceLevelId()));
        }
        
        // Update the job posting
        jobPosting.setRequirementTitle(requestDTO.getRequirementTitle());
        jobPosting.setJobTitle(requestDTO.getJobTitle());
        jobPosting.setJobDesignation(requestDTO.getJobDesignation());
        jobPosting.setJobDescription(requestDTO.getJobDescription());
        jobPosting.setJobFunction(jobFunction);
        jobPosting.setJobType(jobType);
        jobPosting.setLocation(location);
        jobPosting.setDepartment(department);
        jobPosting.setExperienceLevel(experienceLevel);
        jobPosting.setSalaryMin(requestDTO.getSalaryMin());
        jobPosting.setSalaryMax(requestDTO.getSalaryMax());
        jobPosting.setModifiedDate(LocalDateTime.now());
        jobPosting.setActive(requestDTO.isActive());
        
        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        return mapToResponseDTO(updatedJobPosting);
    }

    @Override
    @Transactional
    public void deleteJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + id));
        
        // Soft delete - set isActive to false
        jobPosting.setActive(false);
        jobPosting.setModifiedDate(LocalDateTime.now());
        jobPostingRepository.save(jobPosting);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponseDTO copyJobPosting(Long id) {
        // Get the existing job posting
        JobPosting existingJobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + id));
        
        // Create a DTO with copied data (but clear the ID)
        JobPostingResponseDTO copyDTO = mapToResponseDTO(existingJobPosting);
        copyDTO.setId(null);
        copyDTO.setRequirementTitle("Copy of " + copyDTO.getRequirementTitle());
        
        return copyDTO;
    }

    @Override
    @Transactional
    public JobPostingResponseDTO toggleJobStatus(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + id));
        
        // Toggle the active status
        jobPosting.setActive(!jobPosting.isActive());
        jobPosting.setModifiedDate(LocalDateTime.now());
        
        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        return mapToResponseDTO(updatedJobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<JobPostingResponseDTO> getAllJobPostingsWithAdvancedFilters(
            String search, String title, String requirementTitle, String jobDesignation, String jobDescription,
            Long jobFunctionId, Long jobTypeId, Long locationId, Long departmentId, Long experienceLevelId,
            Boolean isActive, Long createdByUserId,
            LocalDateTime createdDateFrom, LocalDateTime createdDateTo, 
            LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
            BigDecimal salaryMinFrom, BigDecimal salaryMinTo, 
            BigDecimal salaryMaxFrom, BigDecimal salaryMaxTo,
            Pageable pageable, boolean adminView) {
        
        BooleanBuilder predicate = new BooleanBuilder();
        
        // Global search - takes precedence over individual field searches
        if (search != null && !search.trim().isEmpty()) {
            predicate.and(JobPostingPredicate.globalSearch(search));
        } else {
            // Individual field searches (only if no global search)
            if (title != null) {
                predicate.and(JobPostingPredicate.hasTitle(title));
            }
            
            if (requirementTitle != null) {
                predicate.and(JobPostingPredicate.hasRequirementTitle(requirementTitle));
            }
            
            if (jobDesignation != null) {
                predicate.and(JobPostingPredicate.hasJobDesignation(jobDesignation));
            }
            
            if (jobDescription != null) {
                predicate.and(JobPostingPredicate.hasJobDescription(jobDescription));
            }
        }
        
        // Entity filters
        if (jobFunctionId != null) {
            predicate.and(JobPostingPredicate.hasJobFunction(jobFunctionId));
        }
        
        if (jobTypeId != null) {
            predicate.and(JobPostingPredicate.hasJobType(jobTypeId));
        }
        
        if (locationId != null) {
            predicate.and(JobPostingPredicate.hasLocation(locationId));
        }
        
        if (departmentId != null) {
            predicate.and(JobPostingPredicate.hasDepartment(departmentId));
        }
        
        if (experienceLevelId != null) {
            predicate.and(JobPostingPredicate.hasExperienceLevel(experienceLevelId));
        }
        
        if (createdByUserId != null) {
            predicate.and(JobPostingPredicate.hasCreatedByUser(createdByUserId));
        }
        
        // Status filter
        if (isActive != null) {
            predicate.and(JobPostingPredicate.hasActiveStatus(isActive));
        } else if (!adminView) {
            // If not admin view and no specific status filter, show only active jobs
            predicate.and(JobPostingPredicate.isActive());
        }
        
        // Date range filters
        if (createdDateFrom != null || createdDateTo != null) {
            predicate.and(JobPostingPredicate.createdDateBetween(createdDateFrom, createdDateTo));
        }
        
        if (modifiedDateFrom != null || modifiedDateTo != null) {
            predicate.and(JobPostingPredicate.modifiedDateBetween(modifiedDateFrom, modifiedDateTo));
        }
        
        // Salary range filters
        if (salaryMinFrom != null || salaryMinTo != null) {
            predicate.and(JobPostingPredicate.salaryMinBetween(salaryMinFrom, salaryMinTo));
        }
        
        if (salaryMaxFrom != null || salaryMaxTo != null) {
            predicate.and(JobPostingPredicate.salaryMaxBetween(salaryMaxFrom, salaryMaxTo));
        }
        
        return jobPostingRepository.findAll(predicate, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponseDTO> getRecentJobPostings(int limit) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(JobPostingPredicate.isActive());
        
        // Create a pageable for the limit with sorting by created date descending
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"));
        
        Page<JobPosting> recentJobs = jobPostingRepository.findAll(predicate, pageable);
        
        return recentJobs.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private JobPostingResponseDTO mapToResponseDTO(JobPosting jobPosting) {
        return JobPostingResponseDTO.builder()
                .id(jobPosting.getId())
                .requirementTitle(jobPosting.getRequirementTitle())
                .jobTitle(jobPosting.getJobTitle())
                .jobDesignation(jobPosting.getJobDesignation())
                .jobDescription(jobPosting.getJobDescription())
                .jobFunction(mapToJobFunctionDTO(jobPosting.getJobFunction()))
                .jobType(mapToJobTypeDTO(jobPosting.getJobType()))
                .location(mapToLocationDTO(jobPosting.getLocation()))
                .department(jobPosting.getDepartment() != null ? mapToDepartmentDTO(jobPosting.getDepartment()) : null)
                .experienceLevel(jobPosting.getExperienceLevel() != null ? mapToExperienceLevelDTO(jobPosting.getExperienceLevel()) : null)
                .salaryMin(jobPosting.getSalaryMin())
                .salaryMax(jobPosting.getSalaryMax())
                .createdById(jobPosting.getCreatedByUser().getId())
                .createdByName(jobPosting.getCreatedByUser().getName())
                .createdDate(jobPosting.getCreatedDate())
                .modifiedDate(jobPosting.getModifiedDate())
                .isActive(jobPosting.isActive())
                .build();
    }
    
    private JobFunctionDTO mapToJobFunctionDTO(JobFunction jobFunction) {
        return JobFunctionDTO.builder()
                .id(jobFunction.getId())
                .name(jobFunction.getName())
                .active(jobFunction.isActive())
                .build();
    }
    
    private JobTypeDTO mapToJobTypeDTO(JobType jobType) {
        return JobTypeDTO.builder()
                .id(jobType.getId())
                .name(jobType.getName())
                .active(jobType.isActive())
                .build();
    }
    
    private Location mapToLocationDTO(Location location) {
        // Implementation of mapToLocationDTO method
        return location;
    }
    
    private Department mapToDepartmentDTO(Department department) {
        // Implementation of mapToDepartmentDTO method
        return department;
    }
    
    private ExperienceLevel mapToExperienceLevelDTO(ExperienceLevel experienceLevel) {
        // Implementation of mapToExperienceLevelDTO method
        return experienceLevel;
    }
} 