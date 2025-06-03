package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.entity.Role;

import java.util.List;
import java.util.Set;

public interface RoleService {
    
    Role findRoleById(Long id);
    
    Role findRoleByName(String name);
    
    List<Role> getAllRoles();
    
    Role createRole(Role role);
    
    Role updateRole(Role role);
    
    void deleteRole(Long id);
    
    Set<Role> getRolesByIds(Set<Long> roleIds);
} 