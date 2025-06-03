package com.HyundaiAutoever.ATS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String url;
    private Long parentId;
    private Integer displayOrder;
    private boolean canView;
    private boolean canAdd;
    private boolean canEdit;
    private boolean canDelete;
    @Builder.Default    private List<MenuDto> children = new ArrayList<>();
} 