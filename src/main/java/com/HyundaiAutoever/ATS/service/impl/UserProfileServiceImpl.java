package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.UserBasicInfoDTO;
import com.HyundaiAutoever.ATS.dto.UserEducationDTO;
import com.HyundaiAutoever.ATS.dto.UserEmploymentDTO;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.entity.UserBasicInfo;
import com.HyundaiAutoever.ATS.entity.UserEducation;
import com.HyundaiAutoever.ATS.entity.UserEmployment;
import com.HyundaiAutoever.ATS.repository.UserBasicInfoRepository;
import com.HyundaiAutoever.ATS.repository.UserEducationRepository;
import com.HyundaiAutoever.ATS.repository.UserEmploymentRepository;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import com.HyundaiAutoever.ATS.service.UserProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserBasicInfoRepository userBasicInfoRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserEmploymentRepository userEmploymentRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Basic Information Methods
    @Override
    @Transactional(readOnly = true)
    public UserBasicInfoDTO getUserBasicInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserBasicInfo basicInfo = userBasicInfoRepository.findByUserId(userId).orElse(null);

        UserBasicInfoDTO.UserBasicInfoDTOBuilder builder = UserBasicInfoDTO.builder()
                .fullName(user.getName())
                .email(user.getEmail());

        if (basicInfo != null) {
            builder.id(basicInfo.getId())
                    .mobileNumber(basicInfo.getMobileNumber())
                    .gender(basicInfo.getGender())
                    .dateOfBirth(basicInfo.getDateOfBirth())
                    .experienceYears(basicInfo.getExperienceYears())
                    .experienceMonths(basicInfo.getExperienceMonths())
                    .annualSalaryLakhs(basicInfo.getAnnualSalaryLakhs())
                    .annualSalaryThousands(basicInfo.getAnnualSalaryThousands())
                    .jobFunction(basicInfo.getJobFunction())
                    .currentLocation(basicInfo.getCurrentLocation())
                    .preferredLocation(basicInfo.getPreferredLocation())
                    .resumeFilePath(basicInfo.getResumeFilePath());

            // Parse skills JSON
            if (basicInfo.getSkills() != null) {
                try {
                    List<String> skills = objectMapper.readValue(basicInfo.getSkills(), new TypeReference<List<String>>() {});
                    builder.skills(skills);
                } catch (JsonProcessingException e) {
                    log.error("Error parsing skills JSON", e);
                    builder.skills(new ArrayList<>());
                }
            }
        }

        return builder.build();
    }

    @Override
    public UserBasicInfoDTO saveUserBasicInfo(Long userId, UserBasicInfoDTO basicInfoDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserBasicInfo basicInfo = userBasicInfoRepository.findByUserId(userId)
                .orElse(UserBasicInfo.builder().user(user).build());

        // Update fields
        basicInfo.setMobileNumber(basicInfoDTO.getMobileNumber());
        basicInfo.setGender(basicInfoDTO.getGender());
        basicInfo.setDateOfBirth(basicInfoDTO.getDateOfBirth());
        basicInfo.setExperienceYears(basicInfoDTO.getExperienceYears());
        basicInfo.setExperienceMonths(basicInfoDTO.getExperienceMonths());
        basicInfo.setAnnualSalaryLakhs(basicInfoDTO.getAnnualSalaryLakhs());
        basicInfo.setAnnualSalaryThousands(basicInfoDTO.getAnnualSalaryThousands());
        basicInfo.setJobFunction(basicInfoDTO.getJobFunction());
        basicInfo.setCurrentLocation(basicInfoDTO.getCurrentLocation());
        basicInfo.setPreferredLocation(basicInfoDTO.getPreferredLocation());

        // Convert skills to JSON
        if (basicInfoDTO.getSkills() != null) {
            try {
                String skillsJson = objectMapper.writeValueAsString(basicInfoDTO.getSkills());
                basicInfo.setSkills(skillsJson);
            } catch (JsonProcessingException e) {
                log.error("Error converting skills to JSON", e);
            }
        }

        UserBasicInfo saved = userBasicInfoRepository.save(basicInfo);
        return getUserBasicInfo(userId);
    }

    @Override
    public String uploadResume(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = "resume_" + userId + "_" + UUID.randomUUID() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user basic info with file path
            UserBasicInfo basicInfo = userBasicInfoRepository.findByUserId(userId)
                    .orElse(UserBasicInfo.builder()
                            .user(userRepository.findById(userId)
                                    .orElseThrow(() -> new RuntimeException("User not found")))
                            .build());

            basicInfo.setResumeFilePath(filePath.toString());
            userBasicInfoRepository.save(basicInfo);

            return filePath.toString();

        } catch (IOException e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("Failed to upload file");
        }
    }

    // Education Methods
    @Override
    @Transactional(readOnly = true)
    public List<UserEducationDTO> getUserEducation(Long userId) {
        List<UserEducation> educations = userEducationRepository.findByUserIdOrderByPassingYearDesc(userId);
        return educations.stream()
                .map(this::convertToEducationDTO)
                .toList();
    }

    @Override
    public UserEducationDTO saveUserEducation(Long userId, UserEducationDTO educationDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserEducation education = UserEducation.builder()
                .user(user)
                .qualification(educationDTO.getQualification())
                .course(educationDTO.getCourse())
                .specialization(educationDTO.getSpecialization())
                .percentage(educationDTO.getPercentage())
                .collegeSchool(educationDTO.getCollegeSchool())
                .universityBoard(educationDTO.getUniversityBoard())
                .courseType(educationDTO.getCourseType())
                .passingYear(educationDTO.getPassingYear())
                .build();

        UserEducation saved = userEducationRepository.save(education);
        return convertToEducationDTO(saved);
    }

    @Override
    public UserEducationDTO updateUserEducation(Long userId, Long educationId, UserEducationDTO educationDTO) {
        UserEducation education = userEducationRepository.findById(educationId)
                .orElseThrow(() -> new RuntimeException("Education record not found"));

        if (!education.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to education record");
        }

        education.setQualification(educationDTO.getQualification());
        education.setCourse(educationDTO.getCourse());
        education.setSpecialization(educationDTO.getSpecialization());
        education.setPercentage(educationDTO.getPercentage());
        education.setCollegeSchool(educationDTO.getCollegeSchool());
        education.setUniversityBoard(educationDTO.getUniversityBoard());
        education.setCourseType(educationDTO.getCourseType());
        education.setPassingYear(educationDTO.getPassingYear());

        UserEducation saved = userEducationRepository.save(education);
        return convertToEducationDTO(saved);
    }

    @Override
    public void deleteUserEducation(Long userId, Long educationId) {
        UserEducation education = userEducationRepository.findById(educationId)
                .orElseThrow(() -> new RuntimeException("Education record not found"));

        if (!education.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to education record");
        }

        userEducationRepository.delete(education);
    }

    // Employment Methods
    @Override
    @Transactional(readOnly = true)
    public List<UserEmploymentDTO> getUserEmployment(Long userId) {
        List<UserEmployment> employments = userEmploymentRepository.findByUserIdOrderByWorkingSinceDesc(userId);
        return employments.stream()
                .map(this::convertToEmploymentDTO)
                .toList();
    }

    @Override
    public UserEmploymentDTO saveUserEmployment(Long userId, UserEmploymentDTO employmentDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserEmployment employment = UserEmployment.builder()
                .user(user)
                .companyName(employmentDTO.getCompanyName())
                .organizationType(employmentDTO.getOrganizationType())
                .designation(employmentDTO.getDesignation())
                .workingSince(employmentDTO.getWorkingSince())
                .location(employmentDTO.getLocation())
                .build();

        UserEmployment saved = userEmploymentRepository.save(employment);
        return convertToEmploymentDTO(saved);
    }

    @Override
    public UserEmploymentDTO updateUserEmployment(Long userId, Long employmentId, UserEmploymentDTO employmentDTO) {
        UserEmployment employment = userEmploymentRepository.findById(employmentId)
                .orElseThrow(() -> new RuntimeException("Employment record not found"));

        if (!employment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to employment record");
        }

        employment.setCompanyName(employmentDTO.getCompanyName());
        employment.setOrganizationType(employmentDTO.getOrganizationType());
        employment.setDesignation(employmentDTO.getDesignation());
        employment.setWorkingSince(employmentDTO.getWorkingSince());
        employment.setLocation(employmentDTO.getLocation());

        UserEmployment saved = userEmploymentRepository.save(employment);
        return convertToEmploymentDTO(saved);
    }

    @Override
    public void deleteUserEmployment(Long userId, Long employmentId) {
        UserEmployment employment = userEmploymentRepository.findById(employmentId)
                .orElseThrow(() -> new RuntimeException("Employment record not found"));

        if (!employment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to employment record");
        }

        userEmploymentRepository.delete(employment);
    }

    // Helper methods
    private UserEducationDTO convertToEducationDTO(UserEducation education) {
        return UserEducationDTO.builder()
                .id(education.getId())
                .qualification(education.getQualification())
                .course(education.getCourse())
                .specialization(education.getSpecialization())
                .percentage(education.getPercentage())
                .collegeSchool(education.getCollegeSchool())
                .universityBoard(education.getUniversityBoard())
                .courseType(education.getCourseType())
                .passingYear(education.getPassingYear())
                .build();
    }

    private UserEmploymentDTO convertToEmploymentDTO(UserEmployment employment) {
        return UserEmploymentDTO.builder()
                .id(employment.getId())
                .companyName(employment.getCompanyName())
                .organizationType(employment.getOrganizationType())
                .designation(employment.getDesignation())
                .workingSince(employment.getWorkingSince())
                .location(employment.getLocation())
                .build();
    }
} 