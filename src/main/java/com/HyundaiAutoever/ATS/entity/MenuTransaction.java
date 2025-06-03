package com.HyundaiAutoever.ATS.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @JsonIgnoreProperties("menuTransactions")
    private MenuMaster menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnoreProperties("menuTransactions")
    private Role role;

    @Column(nullable = false)
    private boolean canView;

    @Column(nullable = false)
    private boolean canAdd;

    @Column(nullable = false)
    private boolean canEdit;

    @Column(nullable = false)
    private boolean canDelete;
} 