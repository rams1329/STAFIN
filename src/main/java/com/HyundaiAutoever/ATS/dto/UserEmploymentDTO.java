package com.HyundaiAutoever.ATS.dto;

import com.HyundaiAutoever.ATS.entity.UserEmployment;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmploymentDTO {
    
    private Long id;
    private String companyName;
    private UserEmployment.OrganizationType organizationType;
    private String designation;
    private LocalDate workingSince;
    private String location;
} 