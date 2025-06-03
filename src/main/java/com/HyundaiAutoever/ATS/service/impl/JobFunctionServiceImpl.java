package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.JobFunctionDTO;
import com.HyundaiAutoever.ATS.entity.JobFunction;
import com.HyundaiAutoever.ATS.exception.ResourceAlreadyExistsException;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.JobFunctionRepository;
import com.HyundaiAutoever.ATS.service.JobFunctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobFunctionServiceImpl implements JobFunctionService {

    private final JobFunctionRepository jobFunctionRepository;

    @Override
    @Transactional
    public JobFunctionDTO createJobFunction(JobFunctionDTO jobFunctionDTO) {
        if (jobFunctionRepository.existsByNameIgnoreCase(jobFunctionDTO.getName())) {
            throw new ResourceAlreadyExistsException("Job function with name " + jobFunctionDTO.getName() + " already exists");
        }
        
        JobFunction jobFunction = JobFunction.builder()
                .name(jobFunctionDTO.getName())
                .active(jobFunctionDTO.isActive())
                .build();
        
        JobFunction savedJobFunction = jobFunctionRepository.save(jobFunction);
        return mapToDTO(savedJobFunction);
    }

    @Override
    @Transactional(readOnly = true)
    public JobFunctionDTO getJobFunctionById(Long id) {
        JobFunction jobFunction = jobFunctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job function not found with id: " + id));
        
        return mapToDTO(jobFunction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobFunctionDTO> getAllJobFunctions() {
        return jobFunctionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobFunctionDTO> getAllActiveJobFunctions() {
        return jobFunctionRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobFunctionDTO updateJobFunction(Long id, JobFunctionDTO jobFunctionDTO) {
        JobFunction jobFunction = jobFunctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job function not found with id: " + id));
        
        // Check if name is being changed and if it already exists
        if (!jobFunction.getName().equalsIgnoreCase(jobFunctionDTO.getName()) &&
                jobFunctionRepository.existsByNameIgnoreCase(jobFunctionDTO.getName())) {
            throw new ResourceAlreadyExistsException("Job function with name " + jobFunctionDTO.getName() + " already exists");
        }
        
        jobFunction.setName(jobFunctionDTO.getName());
        jobFunction.setActive(jobFunctionDTO.isActive());
        
        JobFunction updatedJobFunction = jobFunctionRepository.save(jobFunction);
        return mapToDTO(updatedJobFunction);
    }

    @Override
    @Transactional
    public void deleteJobFunction(Long id) {
        JobFunction jobFunction = jobFunctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job function not found with id: " + id));
        
        // Soft delete - set active to false
        jobFunction.setActive(false);
        jobFunctionRepository.save(jobFunction);
    }
    
    private JobFunctionDTO mapToDTO(JobFunction jobFunction) {
        return JobFunctionDTO.builder()
                .id(jobFunction.getId())
                .name(jobFunction.getName())
                .active(jobFunction.isActive())
                .build();
    }
} 