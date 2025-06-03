package com.HyundaiAutoever.ATS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_employment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserEmployment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", length = 200, nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", length = 10, nullable = false)
    private OrganizationType organizationType;

    @Column(name = "designation", length = 100, nullable = false)
    private String designation;

    @Column(name = "working_since", nullable = false)
    private LocalDate workingSince;

    @Column(name = "location", length = 100)
    private String location;

    public enum OrganizationType {
        CURRENT, PREVIOUS
    }
} 