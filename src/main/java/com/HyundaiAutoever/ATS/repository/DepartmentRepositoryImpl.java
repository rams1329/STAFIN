package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.Department;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Department> findAllWithFilters(
            String search, String name, String description, Boolean isActive,
            LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
            LocalDateTime modifiedDateFrom, LocalDateTime modifiedDateTo,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> query = cb.createQuery(Department.class);
        Root<Department> root = query.from(Department.class);

        List<Predicate> predicates = new ArrayList<>();

        // Global search
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(root.get("name")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern)
            );
            predicates.add(searchPredicate);
        } else {
            // Individual field searches
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
        }

        // Status filter
        if (isActive != null) {
            predicates.add(cb.equal(root.get("isActive"), isActive));
        }

        // Date range filters
        if (createdDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdDateFrom));
        }
        if (createdDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdDateTo));
        }
        if (modifiedDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("modifiedAt"), modifiedDateFrom));
        }
        if (modifiedDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("modifiedAt"), modifiedDateTo));
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        List<Department> departments = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query for total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Department> countRoot = countQuery.from(Department.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(departments, pageable, total);
    }
} 