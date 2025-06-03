package com.HyundaiAutoever.ATS.dto;

import com.HyundaiAutoever.ATS.entity.UserBasicInfo;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicInfoDTO {
    
    private Long id;
    
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be 10 digits starting with 6, 7, 8, or 9")
    private String mobileNumber;
    private UserBasicInfo.Gender gender;
    private LocalDate dateOfBirth;
    private Integer experienceYears;
    private Integer experienceMonths;
    private BigDecimal annualSalaryLakhs;
    private BigDecimal annualSalaryThousands;
    private List<String> skills;
    private String jobFunction;
    private String currentLocation;
    private String preferredLocation;
    private String resumeFilePath;
    
    // Read-only fields from User entity
    private String fullName;
    private String email;
} 