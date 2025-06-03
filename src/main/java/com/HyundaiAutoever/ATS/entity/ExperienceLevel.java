package com.HyundaiAutoever.ATS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experience_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceLevel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "min_years")
    private Integer minYears;

    @Column(name = "max_years")
    private Integer maxYears;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
} 