package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.JobPosting;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, QuerydslPredicateExecutor<JobPosting> {
    Page<JobPosting> findAll(Predicate predicate, Pageable pageable);
    Optional<JobPosting> findByIdAndIsActiveTrue(Long id);
} 