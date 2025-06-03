package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.ContactMessage;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long>, QuerydslPredicateExecutor<ContactMessage> {
    
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    
    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();
    
    long countByIsReadFalse();
    
    Page<ContactMessage> findAll(Predicate predicate, Pageable pageable);
} 