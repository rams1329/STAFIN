package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.UserBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBasicInfoRepository extends JpaRepository<UserBasicInfo, Long> {
    
    Optional<UserBasicInfo> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
} 