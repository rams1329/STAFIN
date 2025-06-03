package com.HyundaiAutoever.ATS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuAnalysisResponse {
    
    // Current user information
    private String userEmail;
    private String userName;
    private List<String> userRoles;
    
    // Menu visibility analysis
    private List<MenuDto> visibleMenus;
    private List<MenuDto> allMenus;
    private int totalMenuCount;
    private int visibleMenuCount;
    
    // Role-based analysis
    private Map<String, List<MenuDto>> menusByRole;
    private Map<String, Integer> menuCountByRole;
    
    // Menu permissions summary
    private Map<String, Map<String, Boolean>> menuPermissions;
    
    // Administrative insights
    private List<MenuDto> orphanedMenus; // Menus not assigned to any role
    private List<MenuDto> adminOnlyMenus;
    private List<MenuDto> publicMenus;
    
    // Menu tree structure
    private List<MenuDto> menuTree;
    
    // Statistics
    private Map<String, Object> statistics;
} 