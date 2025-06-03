package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.Department;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, QuerydslPredicateExecutor<Department>, DepartmentRepositoryCustom {
    
    @Query("SELECT d FROM Department d WHERE d.isActive = true ORDER BY d.name")
    List<Department> findAllActive();
    
    boolean existsByName(String name);
    
    // Custom method for filtering - to be implemented in a custom repository
    Page<Department> findAllWithFilters(
        String search, String name, String description, Boolean isActive,
        LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
        LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
        Pageable pageable);
} 