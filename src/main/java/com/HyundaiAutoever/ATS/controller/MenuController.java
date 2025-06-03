package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.MenuDto;
import com.HyundaiAutoever.ATS.dto.MenuRoleAssignmentRequest;
import com.HyundaiAutoever.ATS.dto.MenuAnalysisResponse;
import com.HyundaiAutoever.ATS.entity.MenuMaster;
import com.HyundaiAutoever.ATS.entity.MenuTransaction;
import com.HyundaiAutoever.ATS.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuMaster>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuMaster> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuMaster> createMenu(@Valid @RequestBody MenuMaster menu) {
        MenuMaster createdMenu = menuService.createMenu(menu);
        
        // Automatically assign the new menu to admin role with full permissions
        try {
            menuService.assignMenuToAdminRole(createdMenu.getId());
        } catch (Exception e) {
            // Log the error but don't fail the menu creation
            System.err.println("Warning: Failed to assign menu to admin role: " + e.getMessage());
        }
        
        return ResponseEntity.ok(createdMenu);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuMaster> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuMaster menu) {
        menu.setId(id);
        return ResponseEntity.ok(menuService.updateMenu(menu));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("Menu deleted successfully"));
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuDto>> getRoleMenus(@PathVariable Long roleId) {
        return ResponseEntity.ok(menuService.getRoleMenus(roleId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<MenuDto>> getCurrentUserMenus(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(menuService.getUserMenus(email));
    }

    @PostMapping("/{menuId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuTransaction> assignMenuToRole(
            @PathVariable Long menuId,
            @PathVariable Long roleId,
            @RequestParam(defaultValue = "true") boolean canView,
            @RequestParam(defaultValue = "false") boolean canAdd,
            @RequestParam(defaultValue = "false") boolean canEdit,
            @RequestParam(defaultValue = "false") boolean canDelete) {
        
        return ResponseEntity.ok(menuService.assignMenuToRole(menuId, roleId, canView, canAdd, canEdit, canDelete));
    }

    @DeleteMapping("/{menuId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeMenuFromRole(
            @PathVariable Long menuId,
            @PathVariable Long roleId) {
        
        menuService.removeMenuFromRole(menuId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Menu removed from role successfully"));
    }

    @PostMapping("/fix-admin-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> fixAdminMenuAssignments() {
        try {
            int fixedCount = menuService.fixAdminMenuAssignments();
            return ResponseEntity.ok(ApiResponse.success("Fixed " + fixedCount + " menu assignments"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to fix menu assignments: " + e.getMessage()));
        }
    }

    /**
     * Get comprehensive menu analysis for current user/admin
     */
    @GetMapping("/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuAnalysisResponse>> getMenuAnalysis(Authentication authentication) {
        MenuAnalysisResponse analysis = menuService.getMenuAnalysis(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu analysis retrieved successfully", analysis));
    }

    /**
     * Get all available menus with their role mappings
     */
    @GetMapping("/mappings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMenuMappings() {
        Map<String, Object> mappings = menuService.getMenuRoleMappings();
        return ResponseEntity.ok(ApiResponse.success("Menu mappings retrieved successfully", mappings));
    }

    /**
     * Bulk assign menus to role
     */
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkAssignMenusToRole(
            @Valid @RequestBody MenuRoleAssignmentRequest request) {
        int assignedCount = menuService.bulkAssignMenusToRole(request);
        return ResponseEntity.ok(ApiResponse.success("Successfully assigned " + assignedCount + " menus to role"));
    }

    /**
     * Get menu availability for specific role
     */
    @GetMapping("/availability/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMenuAvailabilityForRole(@PathVariable Long roleId) {
        Map<String, Object> availability = menuService.getMenuAvailabilityForRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Menu availability retrieved successfully", availability));
    }

    /**
     * Update menu route
     */
    @PatchMapping("/{id}/route")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuMaster>> updateMenuRoute(
            @PathVariable Long id,
            @RequestBody Map<String, String> routeUpdate) {
        String newRoute = routeUpdate.get("url");
        MenuMaster updatedMenu = menuService.updateMenuRoute(id, newRoute);
        return ResponseEntity.ok(ApiResponse.success("Menu route updated successfully", updatedMenu));
    }

    /**
     * Toggle menu active status
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuMaster>> toggleMenuStatus(@PathVariable Long id) {
        MenuMaster menu = menuService.toggleMenuStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Menu status updated successfully", menu));
    }

    /**
     * Get menu tree structure
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MenuDto>>> getMenuTree() {
        List<MenuDto> menuTree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success("Menu tree retrieved successfully", menuTree));
    }

    /**
     * Reorder menus
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reorderMenus(@RequestBody List<Map<String, Object>> menuOrders) {
        menuService.reorderMenus(menuOrders);
        return ResponseEntity.ok(ApiResponse.success("Menus reordered successfully"));
    }
} 