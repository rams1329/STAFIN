package com.HyundaiAutoever.ATS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobFunction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
} 