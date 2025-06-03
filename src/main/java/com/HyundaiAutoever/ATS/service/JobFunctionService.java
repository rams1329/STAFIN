package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.JobFunctionDTO;
import java.util.List;

public interface JobFunctionService {
    JobFunctionDTO createJobFunction(JobFunctionDTO jobFunctionDTO);
    JobFunctionDTO getJobFunctionById(Long id);
    List<JobFunctionDTO> getAllJobFunctions();
    List<JobFunctionDTO> getAllActiveJobFunctions();
    JobFunctionDTO updateJobFunction(Long id, JobFunctionDTO jobFunctionDTO);
    void deleteJobFunction(Long id);
} 