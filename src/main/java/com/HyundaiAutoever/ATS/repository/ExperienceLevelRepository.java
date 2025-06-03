package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.ExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceLevelRepository extends JpaRepository<ExperienceLevel, Long> {
    
    @Query("SELECT e FROM ExperienceLevel e WHERE e.isActive = true ORDER BY e.minYears")
    List<ExperienceLevel> findAllActive();
    
    boolean existsByName(String name);
} 