package com.HyundaiAutoever.ATS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

        @ManyToMany(mappedBy = "roles")    @JsonIgnoreProperties("roles")    @Builder.Default    private Set<User> users = new HashSet<>();

        @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)    @JsonIgnoreProperties("role")    @Builder.Default    private Set<MenuTransaction> menuTransactions = new HashSet<>();
} 