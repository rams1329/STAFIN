package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.UserBasicInfoDTO;
import com.HyundaiAutoever.ATS.dto.UserEducationDTO;
import com.HyundaiAutoever.ATS.dto.UserEmploymentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserProfileService {
    
    // Basic Information
    UserBasicInfoDTO getUserBasicInfo(Long userId);
    UserBasicInfoDTO saveUserBasicInfo(Long userId, UserBasicInfoDTO basicInfoDTO);
    String uploadResume(Long userId, MultipartFile file);
    
    // Education Information
    List<UserEducationDTO> getUserEducation(Long userId);
    UserEducationDTO saveUserEducation(Long userId, UserEducationDTO educationDTO);
    UserEducationDTO updateUserEducation(Long userId, Long educationId, UserEducationDTO educationDTO);
    void deleteUserEducation(Long userId, Long educationId);
    
    // Employment Information
    List<UserEmploymentDTO> getUserEmployment(Long userId);
    UserEmploymentDTO saveUserEmployment(Long userId, UserEmploymentDTO employmentDTO);
    UserEmploymentDTO updateUserEmployment(Long userId, Long employmentId, UserEmploymentDTO employmentDTO);
    void deleteUserEmployment(Long userId, Long employmentId);
} 