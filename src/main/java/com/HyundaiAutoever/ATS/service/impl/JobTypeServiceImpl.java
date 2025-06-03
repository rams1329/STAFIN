package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.JobTypeDTO;
import com.HyundaiAutoever.ATS.entity.JobType;
import com.HyundaiAutoever.ATS.exception.ResourceAlreadyExistsException;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.JobTypeRepository;
import com.HyundaiAutoever.ATS.service.JobTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobTypeServiceImpl implements JobTypeService {

    private final JobTypeRepository jobTypeRepository;

    @Override
    @Transactional
    public JobTypeDTO createJobType(JobTypeDTO jobTypeDTO) {
        if (jobTypeRepository.existsByNameIgnoreCase(jobTypeDTO.getName())) {
            throw new ResourceAlreadyExistsException("Job type with name " + jobTypeDTO.getName() + " already exists");
        }
        
        JobType jobType = JobType.builder()
                .name(jobTypeDTO.getName())
                .active(jobTypeDTO.isActive())
                .build();
        
        JobType savedJobType = jobTypeRepository.save(jobType);
        return mapToDTO(savedJobType);
    }

    @Override
    @Transactional(readOnly = true)
    public JobTypeDTO getJobTypeById(Long id) {
        JobType jobType = jobTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job type not found with id: " + id));
        
        return mapToDTO(jobType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTypeDTO> getAllJobTypes() {
        return jobTypeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTypeDTO> getAllActiveJobTypes() {
        return jobTypeRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobTypeDTO updateJobType(Long id, JobTypeDTO jobTypeDTO) {
        JobType jobType = jobTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job type not found with id: " + id));
        
        // Check if name is being changed and if it already exists
        if (!jobType.getName().equalsIgnoreCase(jobTypeDTO.getName()) &&
                jobTypeRepository.existsByNameIgnoreCase(jobTypeDTO.getName())) {
            throw new ResourceAlreadyExistsException("Job type with name " + jobTypeDTO.getName() + " already exists");
        }
        
        jobType.setName(jobTypeDTO.getName());
        jobType.setActive(jobTypeDTO.isActive());
        
        JobType updatedJobType = jobTypeRepository.save(jobType);
        return mapToDTO(updatedJobType);
    }

    @Override
    @Transactional
    public void deleteJobType(Long id) {
        JobType jobType = jobTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job type not found with id: " + id));
        
        // Soft delete - set active to false
        jobType.setActive(false);
        jobTypeRepository.save(jobType);
    }
    
    private JobTypeDTO mapToDTO(JobType jobType) {
        return JobTypeDTO.builder()
                .id(jobType.getId())
                .name(jobType.getName())
                .active(jobType.isActive())
                .build();
    }
} 