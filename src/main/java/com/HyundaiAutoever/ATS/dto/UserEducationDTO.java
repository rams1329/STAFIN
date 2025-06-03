package com.HyundaiAutoever.ATS.dto;

import com.HyundaiAutoever.ATS.entity.UserEducation;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEducationDTO {
    
    private Long id;
    private String qualification;
    private String course;
    private String specialization;
    private Double percentage;
    private String collegeSchool;
    private String universityBoard;
    private UserEducation.CourseType courseType;
    private Integer passingYear;
} 