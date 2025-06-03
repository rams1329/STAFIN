package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.User;

import java.util.List;

public interface UserRepositoryCustom {
    
    List<User> findUsersByRoleName(String roleName);
    
    List<User> searchUsers(String keyword);
} 