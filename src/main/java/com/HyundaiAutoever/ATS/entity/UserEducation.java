package com.HyundaiAutoever.ATS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_education")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserEducation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "qualification", length = 100, nullable = false)
    private String qualification;

    @Column(name = "course", length = 100, nullable = false)
    private String course;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "college_school", length = 200)
    private String collegeSchool;

    @Column(name = "university_board", length = 200)
    private String universityBoard;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 20)
    private CourseType courseType;

    @Column(name = "passing_year")
    private Integer passingYear;

    public enum CourseType {
        FULL_TIME, PART_TIME, CORRESPONDENCE
    }
} 