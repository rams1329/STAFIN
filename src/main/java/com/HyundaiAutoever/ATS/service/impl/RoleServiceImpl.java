package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.entity.Role;
import com.HyundaiAutoever.ATS.exception.BadRequestException;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.RoleRepository;
import com.HyundaiAutoever.ATS.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new BadRequestException("Role name already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        Role existingRole = findRoleById(role.getId());
        
        if (!existingRole.getName().equals(role.getName()) && 
                roleRepository.existsByName(role.getName())) {
            throw new BadRequestException("Role name already exists");
        }
        
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = findRoleById(id);
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getRolesByIds(Set<Long> roleIds) {
        return roleIds.stream()
                .map(this::findRoleById)
                .collect(Collectors.toSet());
    }
} 