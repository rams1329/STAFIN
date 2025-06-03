package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.MenuDto;
import com.HyundaiAutoever.ATS.dto.MenuRoleAssignmentRequest;
import com.HyundaiAutoever.ATS.dto.MenuAnalysisResponse;
import com.HyundaiAutoever.ATS.entity.MenuMaster;
import com.HyundaiAutoever.ATS.entity.MenuTransaction;
import com.HyundaiAutoever.ATS.entity.Role;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.MenuMasterRepository;
import com.HyundaiAutoever.ATS.repository.MenuTransactionRepository;
import com.HyundaiAutoever.ATS.service.MenuService;
import com.HyundaiAutoever.ATS.service.RoleService;
import com.HyundaiAutoever.ATS.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMasterRepository menuMasterRepository;
    private final MenuTransactionRepository menuTransactionRepository;
    private final UserService userService;
    private final RoleService roleService;

    @Override
    @Transactional(readOnly = true)
    public List<MenuMaster> getAllMenus() {
        return menuMasterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuMaster getMenuById(Long id) {
        return menuMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
    }

    @Override
    @Transactional
    public MenuMaster createMenu(MenuMaster menu) {
        return menuMasterRepository.save(menu);
    }

    @Override
    @Transactional
    public MenuMaster updateMenu(MenuMaster menu) {
        getMenuById(menu.getId()); // Validate menu exists
        return menuMasterRepository.save(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        MenuMaster menu = getMenuById(id);
        menuMasterRepository.delete(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenus(String email) {
        List<MenuMaster> userMenus = menuMasterRepository.findMenusByUserEmail(email);
        List<MenuDto> menuDtos = convertToMenuDtoList(userMenus, email);
        return buildMenuTree(menuDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getRoleMenus(Long roleId) {
        Role role = roleService.findRoleById(roleId);
        List<MenuTransaction> menuTransactions = menuTransactionRepository.findByRole(role);
        
        List<MenuDto> menuDtos = menuTransactions.stream()
                .filter(transaction -> transaction.isCanView())
                .map(transaction -> {
                    MenuMaster menu = transaction.getMenu();
                    return MenuDto.builder()
                            .id(menu.getId())
                            .name(menu.getName())
                            .description(menu.getDescription())
                            .icon(menu.getIcon())
                            .url(menu.getUrl())
                            .parentId(menu.getParentId())
                            .displayOrder(menu.getDisplayOrder())
                            .canView(transaction.isCanView())
                            .canAdd(transaction.isCanAdd())
                            .canEdit(transaction.isCanEdit())
                            .canDelete(transaction.isCanDelete())
                            .build();
                })
                .collect(Collectors.toList());
        
        return buildMenuTree(menuDtos);
    }

    @Override
    @Transactional
    public MenuTransaction assignMenuToRole(Long menuId, Long roleId, boolean canView, boolean canAdd, boolean canEdit, boolean canDelete) {
        MenuMaster menu = getMenuById(menuId);
        Role role = roleService.findRoleById(roleId);
        
        MenuTransaction transaction = menuTransactionRepository.findByMenuAndRole(menu, role)
                .orElse(MenuTransaction.builder()
                        .menu(menu)
                        .role(role)
                        .build());
        
        transaction.setCanView(canView);
        transaction.setCanAdd(canAdd);
        transaction.setCanEdit(canEdit);
        transaction.setCanDelete(canDelete);
        
        return menuTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void removeMenuFromRole(Long menuId, Long roleId) {
        MenuMaster menu = getMenuById(menuId);
        Role role = roleService.findRoleById(roleId);
        
        menuTransactionRepository.findByMenuAndRole(menu, role)
                .ifPresent(menuTransactionRepository::delete);
    }

    @Override
    @Transactional
    public void assignMenuToAdminRole(Long menuId) {
        MenuMaster menu = getMenuById(menuId);
        
        try {
            Role adminRole = roleService.findRoleByName("ROLE_ADMIN");
            assignMenuToRole(menuId, adminRole.getId(), true, true, true, true);
        } catch (Exception e) {
            System.err.println("Failed to assign menu to admin role: " + e.getMessage());
            throw new RuntimeException("Failed to assign menu to admin role", e);
        }
    }

    @Override
    @Transactional
    public int fixAdminMenuAssignments() {
        try {
            Role adminRole = roleService.findRoleByName("ROLE_ADMIN");
            List<MenuMaster> allMenus = getAllMenus();
            int fixedCount = 0;
            
            for (MenuMaster menu : allMenus) {
                try {
                    assignMenuToRole(menu.getId(), adminRole.getId(), true, true, true, true);
                    fixedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to assign menu " + menu.getName() + " to admin role: " + e.getMessage());
                }
            }
            
            return fixedCount;
        } catch (Exception e) {
            System.err.println("Failed to fix admin menu assignments: " + e.getMessage());
            throw new RuntimeException("Failed to fix admin menu assignments", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> buildMenuTree(List<MenuDto> menus) {
        Map<Long, MenuDto> menuMap = new HashMap<>();
        List<MenuDto> rootMenus = new ArrayList<>();
        
        // First pass: create menu map
        for (MenuDto menu : menus) {
            menuMap.put(menu.getId(), menu);
        }
        
        // Second pass: build tree structure
        for (MenuDto menu : menus) {
            if (menu.getParentId() == null) {
                rootMenus.add(menu);
            } else {
                MenuDto parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                }
            }
        }
        
        return rootMenus;
    }

    @Override
    @Transactional(readOnly = true)
    public MenuAnalysisResponse getMenuAnalysis(String userEmail) {
        User user = userService.findUserByEmail(userEmail);
        List<MenuMaster> allMenus = getAllMenus();
        List<MenuDto> userMenus = getUserMenus(userEmail);
        
        // Role analysis
        List<String> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        Map<String, List<MenuDto>> menusByRole = new HashMap<>();
        Map<String, Integer> menuCountByRole = new HashMap<>();
        
        for (Role role : user.getRoles()) {
            List<MenuDto> roleMenus = getRoleMenus(role.getId());
            menusByRole.put(role.getName(), roleMenus);
            menuCountByRole.put(role.getName(), roleMenus.size());
        }
        
        // Menu permissions summary
        Map<String, Map<String, Boolean>> menuPermissions = new HashMap<>();
        for (MenuDto menu : userMenus) {
            Map<String, Boolean> permissions = new HashMap<>();
            permissions.put("view", menu.isCanView());
            permissions.put("add", menu.isCanAdd());
            permissions.put("edit", menu.isCanEdit());
            permissions.put("delete", menu.isCanDelete());
            menuPermissions.put(menu.getName(), permissions);
        }
        
        // Administrative insights
        List<MenuDto> orphanedMenus = findOrphanedMenus();
        List<MenuDto> adminOnlyMenus = findAdminOnlyMenus();
        
        // Statistics
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalMenus", allMenus.size());
        statistics.put("visibleMenus", userMenus.size());
        statistics.put("menuCoverage", userMenus.size() / (double) allMenus.size() * 100);
        statistics.put("orphanedMenus", orphanedMenus.size());
        statistics.put("adminOnlyMenus", adminOnlyMenus.size());
        
        return MenuAnalysisResponse.builder()
                .userEmail(user.getEmail())
                .userName(user.getName())
                .userRoles(userRoles)
                .visibleMenus(userMenus)
                .allMenus(convertToMenuDtoList(allMenus, userEmail))
                .totalMenuCount(allMenus.size())
                .visibleMenuCount(userMenus.size())
                .menusByRole(menusByRole)
                .menuCountByRole(menuCountByRole)
                .menuPermissions(menuPermissions)
                .orphanedMenus(orphanedMenus)
                .adminOnlyMenus(adminOnlyMenus)
                .publicMenus(new ArrayList<>()) // Add implementation if needed
                .menuTree(getMenuTree())
                .statistics(statistics)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMenuRoleMappings() {
        Map<String, Object> mappings = new HashMap<>();
        List<MenuMaster> allMenus = getAllMenus();
        List<Role> allRoles = roleService.getAllRoles();
        
        // Menu to roles mapping
        Map<String, List<String>> menuToRoles = new HashMap<>();
        for (MenuMaster menu : allMenus) {
            List<MenuTransaction> transactions = menuTransactionRepository.findByMenu(menu);
            List<String> assignedRoles = transactions.stream()
                    .filter(MenuTransaction::isCanView)
                    .map(transaction -> transaction.getRole().getName())
                    .collect(Collectors.toList());
            menuToRoles.put(menu.getName(), assignedRoles);
        }
        
        // Role to menus mapping
        Map<String, List<String>> roleToMenus = new HashMap<>();
        for (Role role : allRoles) {
            List<MenuTransaction> transactions = menuTransactionRepository.findByRole(role);
            List<String> assignedMenus = transactions.stream()
                    .filter(MenuTransaction::isCanView)
                    .map(transaction -> transaction.getMenu().getName())
                    .collect(Collectors.toList());
            roleToMenus.put(role.getName(), assignedMenus);
        }
        
        mappings.put("menuToRoles", menuToRoles);
        mappings.put("roleToMenus", roleToMenus);
        mappings.put("totalMenus", allMenus.size());
        mappings.put("totalRoles", allRoles.size());
        
        return mappings;
    }

    @Override
    @Transactional
    public int bulkAssignMenusToRole(MenuRoleAssignmentRequest request) {
        Role role = roleService.findRoleById(request.getRoleId());
        int assignedCount = 0;
        
        for (Long menuId : request.getMenuIds()) {
            try {
                assignMenuToRole(menuId, role.getId(), 
                    request.isCanView(), 
                    request.isCanAdd(), 
                    request.isCanEdit(), 
                    request.isCanDelete());
                assignedCount++;
            } catch (Exception e) {
                System.err.println("Failed to assign menu " + menuId + " to role " + role.getName() + ": " + e.getMessage());
            }
        }
        
        return assignedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMenuAvailabilityForRole(Long roleId) {
        Role role = roleService.findRoleById(roleId);
        List<MenuMaster> allMenus = getAllMenus();
        List<MenuTransaction> roleTransactions = menuTransactionRepository.findByRole(role);
        
        Map<String, Object> availability = new HashMap<>();
        List<Map<String, Object>> menuAvailability = new ArrayList<>();
        
        for (MenuMaster menu : allMenus) {
            Map<String, Object> menuInfo = new HashMap<>();
            menuInfo.put("menuId", menu.getId());
            menuInfo.put("menuName", menu.getName());
            menuInfo.put("menuUrl", menu.getUrl());
            
            // Find transaction for this menu and role
            MenuTransaction transaction = roleTransactions.stream()
                    .filter(t -> t.getMenu().getId().equals(menu.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (transaction != null) {
                menuInfo.put("assigned", true);
                menuInfo.put("canView", transaction.isCanView());
                menuInfo.put("canAdd", transaction.isCanAdd());
                menuInfo.put("canEdit", transaction.isCanEdit());
                menuInfo.put("canDelete", transaction.isCanDelete());
            } else {
                menuInfo.put("assigned", false);
                menuInfo.put("canView", false);
                menuInfo.put("canAdd", false);
                menuInfo.put("canEdit", false);
                menuInfo.put("canDelete", false);
            }
            
            menuAvailability.add(menuInfo);
        }
        
        availability.put("roleName", role.getName());
        availability.put("roleId", role.getId());
        availability.put("menus", menuAvailability);
        availability.put("totalMenus", allMenus.size());
        availability.put("assignedMenus", roleTransactions.size());
        
        return availability;
    }

    @Override
    @Transactional
    public MenuMaster updateMenuRoute(Long menuId, String newRoute) {
        MenuMaster menu = getMenuById(menuId);
        menu.setUrl(newRoute);
        return menuMasterRepository.save(menu);
    }

    @Override
    @Transactional
    public MenuMaster toggleMenuStatus(Long menuId) {
        MenuMaster menu = getMenuById(menuId);
        menu.setActive(!menu.isActive());
        return menuMasterRepository.save(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getMenuTree() {
        List<MenuMaster> allMenus = menuMasterRepository.findByActiveTrue();
        List<MenuDto> menuDtos = allMenus.stream()
                .map(this::convertToMenuDto)
                .collect(Collectors.toList());
        return buildMenuTree(menuDtos);
    }

    @Override
    @Transactional
    public void reorderMenus(List<Map<String, Object>> menuOrders) {
        for (Map<String, Object> menuOrder : menuOrders) {
            Long menuId = Long.valueOf(menuOrder.get("id").toString());
            Integer displayOrder = Integer.valueOf(menuOrder.get("displayOrder").toString());
            
            MenuMaster menu = getMenuById(menuId);
            menu.setDisplayOrder(displayOrder);
            menuMasterRepository.save(menu);
        }
    }

    private List<MenuDto> findOrphanedMenus() {
        List<MenuMaster> allMenus = getAllMenus();
        return allMenus.stream()
                .filter(menu -> menuTransactionRepository.findByMenu(menu).isEmpty())
                .map(this::convertToMenuDto)
                .collect(Collectors.toList());
    }

    private List<MenuDto> findAdminOnlyMenus() {
        try {
            Role adminRole = roleService.findRoleByName("ROLE_ADMIN");
            List<MenuTransaction> adminTransactions = menuTransactionRepository.findByRole(adminRole);
            
            return adminTransactions.stream()
                    .filter(transaction -> {
                        // Check if this menu is only assigned to admin role
                        List<MenuTransaction> allTransactions = menuTransactionRepository.findByMenu(transaction.getMenu());
                        return allTransactions.size() == 1 && allTransactions.get(0).getRole().equals(adminRole);
                    })
                    .map(transaction -> convertToMenuDto(transaction.getMenu()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private MenuDto convertToMenuDto(MenuMaster menu) {
        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .icon(menu.getIcon())
                .url(menu.getUrl())
                .parentId(menu.getParentId())
                .displayOrder(menu.getDisplayOrder())
                .build();
    }

    private List<MenuDto> convertToMenuDtoList(List<MenuMaster> menus, String userEmail) {
        User user = userService.findUserByEmail(userEmail);
        
        return menus.stream()
                .map(menu -> {
                    MenuDto menuDto = MenuDto.builder()
                            .id(menu.getId())
                            .name(menu.getName())
                            .description(menu.getDescription())
                            .icon(menu.getIcon())
                            .url(menu.getUrl())
                            .parentId(menu.getParentId())
                            .displayOrder(menu.getDisplayOrder())
                            .build();
                    
                    // Find the permissions for this menu from user's roles
                    user.getRoles().stream()
                            .flatMap(role -> menuTransactionRepository.findByMenuAndRole(menu, role).stream())
                            .findFirst()
                            .ifPresent(transaction -> {
                                menuDto.setCanView(transaction.isCanView());
                                menuDto.setCanAdd(transaction.isCanAdd());
                                menuDto.setCanEdit(transaction.isCanEdit());
                                menuDto.setCanDelete(transaction.isCanDelete());
                            });
                    
                    return menuDto;
                })
                .collect(Collectors.toList());
    }
} 