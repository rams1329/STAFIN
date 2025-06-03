package com.HyundaiAutoever.ATS.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Configuration;

/**
 * Note: QueryDSL has been replaced with standard JPA queries
 * to avoid Q-class generation issues. This configuration class
 * is kept for reference but is not actively used.
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;
    
    // The JPAQueryFactory bean is removed since we're using standard JPA now
} 