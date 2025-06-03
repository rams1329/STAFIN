package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.JobFunction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobFunctionRepository extends JpaRepository<JobFunction, Long>, JobFunctionRepositoryCustom {
    
    @Query("SELECT jf FROM JobFunction jf WHERE jf.active = true ORDER BY jf.name")
    List<JobFunction> findAllActive();
    
    List<JobFunction> findByActiveTrue();
    
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    
    // Custom method for filtering - to be implemented in a custom repository
    Page<JobFunction> findAllWithFilters(
        String search, String name, String description, Boolean isActive,
        LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
        LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
        Pageable pageable);
} 