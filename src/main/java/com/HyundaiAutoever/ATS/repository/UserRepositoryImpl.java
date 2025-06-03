package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findUsersByRoleName(String roleName) {
        String jpql = "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName";
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("roleName", roleName);
        return query.getResultList();
    }

    @Override
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return entityManager.createQuery("SELECT u FROM User u ORDER BY u.name ASC", User.class)
                    .getResultList();
        }
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);
        
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        
        Predicate namePredicate = cb.like(cb.lower(user.get("name")), searchPattern);
        Predicate emailPredicate = cb.like(cb.lower(user.get("email")), searchPattern);
        
        cq.where(cb.or(namePredicate, emailPredicate));
        cq.orderBy(cb.asc(user.get("name")));
        
        return entityManager.createQuery(cq).getResultList();
    }
} 