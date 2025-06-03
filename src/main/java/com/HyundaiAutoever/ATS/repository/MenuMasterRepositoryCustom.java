package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.MenuMaster;

import java.util.List;

public interface MenuMasterRepositoryCustom {
    
    List<MenuMaster> findMenusByUserEmail(String email);
    
    List<MenuMaster> findMenusByRoleIds(List<Long> roleIds);
} 