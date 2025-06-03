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
@Table(name = "menu_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(length = 255)
    private String url;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("menu")
    @Builder.Default    private Set<MenuTransaction> menuTransactions = new HashSet<>();
} 