package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.MenuDto;
import com.HyundaiAutoever.ATS.dto.MenuRoleAssignmentRequest;
import com.HyundaiAutoever.ATS.dto.MenuAnalysisResponse;
import com.HyundaiAutoever.ATS.entity.MenuMaster;
import com.HyundaiAutoever.ATS.entity.MenuTransaction;

import java.util.List;
import java.util.Map;

public interface MenuService {
    
    List<MenuMaster> getAllMenus();
    
    MenuMaster getMenuById(Long id);
    
    MenuMaster createMenu(MenuMaster menu);
    
    MenuMaster updateMenu(MenuMaster menu);
    
    void deleteMenu(Long id);
    
    List<MenuDto> getUserMenus(String email);
    
    List<MenuDto> getRoleMenus(Long roleId);
    
    MenuTransaction assignMenuToRole(Long menuId, Long roleId, boolean canView, boolean canAdd, boolean canEdit, boolean canDelete);
    
    void removeMenuFromRole(Long menuId, Long roleId);
    
    void assignMenuToAdminRole(Long menuId);
    
    int fixAdminMenuAssignments();
    
    List<MenuDto> buildMenuTree(List<MenuDto> menus);
    
    MenuAnalysisResponse getMenuAnalysis(String userEmail);
    
    Map<String, Object> getMenuRoleMappings();
    
    int bulkAssignMenusToRole(MenuRoleAssignmentRequest request);
    
    Map<String, Object> getMenuAvailabilityForRole(Long roleId);
    
    MenuMaster updateMenuRoute(Long menuId, String newRoute);
    
    MenuMaster toggleMenuStatus(Long menuId);
    
    List<MenuDto> getMenuTree();
    
    void reorderMenus(List<Map<String, Object>> menuOrders);
} 