package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.UserEmployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEmploymentRepository extends JpaRepository<UserEmployment, Long> {
    
    List<UserEmployment> findByUserIdOrderByWorkingSinceDesc(Long userId);
    
    void deleteByUserId(Long userId);
} 