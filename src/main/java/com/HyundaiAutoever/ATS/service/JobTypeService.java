package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.JobTypeDTO;
import java.util.List;

public interface JobTypeService {
    JobTypeDTO createJobType(JobTypeDTO jobTypeDTO);
    JobTypeDTO getJobTypeById(Long id);
    List<JobTypeDTO> getAllJobTypes();
    List<JobTypeDTO> getAllActiveJobTypes();
    JobTypeDTO updateJobType(Long id, JobTypeDTO jobTypeDTO);
    void deleteJobType(Long id);
} 