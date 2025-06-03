package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    List<JobType> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
} 