package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.MenuMaster;
import com.HyundaiAutoever.ATS.entity.MenuTransaction;
import com.HyundaiAutoever.ATS.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuTransactionRepository extends JpaRepository<MenuTransaction, Long> {
    
    List<MenuTransaction> findByRole(Role role);
    
    List<MenuTransaction> findByMenu(MenuMaster menu);
    
    Optional<MenuTransaction> findByMenuAndRole(MenuMaster menu, Role role);
    
    void deleteByRole(Role role);
    
    void deleteByMenu(MenuMaster menu);
} 