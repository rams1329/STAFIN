package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.MenuMaster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class MenuMasterRepositoryImpl implements MenuMasterRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MenuMaster> findMenusByUserEmail(String email) {
        String jpql = "SELECT DISTINCT m FROM MenuMaster m " +
                "JOIN MenuTransaction mt ON mt.menu = m " +
                "JOIN mt.role r " +
                "JOIN r.users u " +
                "WHERE u.email = :email " +
                "AND mt.canView = true " +
                "AND m.active = true " +
                "ORDER BY CASE WHEN m.parentId IS NULL THEN 0 ELSE 1 END, m.parentId, m.displayOrder";
        
        TypedQuery<MenuMaster> query = entityManager.createQuery(jpql, MenuMaster.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    @Override
    public List<MenuMaster> findMenusByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        
        String jpql = "SELECT DISTINCT m FROM MenuMaster m " +
                "JOIN MenuTransaction mt ON mt.menu = m " +
                "WHERE mt.role.id IN :roleIds " +
                "AND mt.canView = true " +
                "AND m.active = true " +
                "ORDER BY CASE WHEN m.parentId IS NULL THEN 0 ELSE 1 END, m.parentId, m.displayOrder";
        
        TypedQuery<MenuMaster> query = entityManager.createQuery(jpql, MenuMaster.class);
        query.setParameter("roleIds", roleIds);
        return query.getResultList();
    }
} 