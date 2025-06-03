package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, Long> {
    
    List<UserEducation> findByUserIdOrderByPassingYearDesc(Long userId);
    
    void deleteByUserId(Long userId);
} 