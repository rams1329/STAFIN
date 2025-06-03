package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.MenuMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuMasterRepository extends JpaRepository<MenuMaster, Long>, MenuMasterRepositoryCustom {
    
    List<MenuMaster> findByParentIdIsNullOrderByDisplayOrder();
    
    List<MenuMaster> findByParentIdOrderByDisplayOrder(Long parentId);
    
    List<MenuMaster> findByActiveTrue();
} 