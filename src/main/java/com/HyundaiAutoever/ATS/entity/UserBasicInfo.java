package com.HyundaiAutoever.ATS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_basic_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserBasicInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "mobile_number", length = 15)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be 10 digits starting with 6, 7, 8, or 9")
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "experience_months")
    private Integer experienceMonths;

    @Column(name = "annual_salary_lakhs")
    private BigDecimal annualSalaryLakhs;

    @Column(name = "annual_salary_thousands")
    private BigDecimal annualSalaryThousands;

    @Column(name = "skills", length = 1000)
    private String skills; // JSON array of skills as string

    @Column(name = "job_function", length = 100)
    private String jobFunction;

    @Column(name = "current_location", length = 100)
    private String currentLocation;

    @Column(name = "preferred_location", length = 100)
    private String preferredLocation;

    @Column(name = "resume_file_path", length = 500)
    private String resumeFilePath;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
} 