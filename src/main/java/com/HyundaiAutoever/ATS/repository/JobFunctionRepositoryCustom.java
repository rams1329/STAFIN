package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.JobFunction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface JobFunctionRepositoryCustom {
    Page<JobFunction> findAllWithFilters(
        String search, String name, String description, Boolean isActive,
        LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
        LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
        Pageable pageable);
} 