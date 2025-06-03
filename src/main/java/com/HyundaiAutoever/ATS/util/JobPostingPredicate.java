package com.HyundaiAutoever.ATS.util;

import com.HyundaiAutoever.ATS.entity.QJobPosting;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobPostingPredicate {
    
    // Global search across multiple fields
    public static BooleanExpression globalSearch(String search) {
        if (ObjectUtils.isEmpty(search)) {
            return null;
        }
        
        return QJobPosting.jobPosting.jobTitle.containsIgnoreCase(search)
                .or(QJobPosting.jobPosting.requirementTitle.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.jobDesignation.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.jobDescription.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.location.name.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.department.name.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.jobFunction.name.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.jobType.name.containsIgnoreCase(search))
                .or(QJobPosting.jobPosting.experienceLevel.name.containsIgnoreCase(search));
    }
    
    // Individual field searches
    public static BooleanExpression hasTitle(String title) {
        return ObjectUtils.isEmpty(title) ? null : QJobPosting.jobPosting.jobTitle.containsIgnoreCase(title);
    }
    
    public static BooleanExpression hasRequirementTitle(String requirementTitle) {
        return ObjectUtils.isEmpty(requirementTitle) ? null : QJobPosting.jobPosting.requirementTitle.containsIgnoreCase(requirementTitle);
    }
    
    public static BooleanExpression hasJobDesignation(String jobDesignation) {
        return ObjectUtils.isEmpty(jobDesignation) ? null : QJobPosting.jobPosting.jobDesignation.containsIgnoreCase(jobDesignation);
    }
    
    public static BooleanExpression hasJobDescription(String jobDescription) {
        return ObjectUtils.isEmpty(jobDescription) ? null : QJobPosting.jobPosting.jobDescription.containsIgnoreCase(jobDescription);
    }
    
    // Entity filters
    public static BooleanExpression hasJobFunction(Long jobFunctionId) {
        return jobFunctionId == null ? null : QJobPosting.jobPosting.jobFunction.id.eq(jobFunctionId);
    }
    
    public static BooleanExpression hasJobType(Long jobTypeId) {
        return jobTypeId == null ? null : QJobPosting.jobPosting.jobType.id.eq(jobTypeId);
    }
    
    public static BooleanExpression hasLocation(Long locationId) {
        return locationId == null ? null : QJobPosting.jobPosting.location.id.eq(locationId);
    }
    
    public static BooleanExpression hasDepartment(Long departmentId) {
        return departmentId == null ? null : QJobPosting.jobPosting.department.id.eq(departmentId);
    }
    
    public static BooleanExpression hasExperienceLevel(Long experienceLevelId) {
        return experienceLevelId == null ? null : QJobPosting.jobPosting.experienceLevel.id.eq(experienceLevelId);
    }
    
    public static BooleanExpression hasCreatedByUser(Long createdByUserId) {
        return createdByUserId == null ? null : QJobPosting.jobPosting.createdByUser.id.eq(createdByUserId);
    }
    
    // Status filter
    public static BooleanExpression isActive() {
        return QJobPosting.jobPosting.isActive.isTrue();
    }
    
    public static BooleanExpression hasActiveStatus(Boolean isActive) {
        return isActive == null ? null : QJobPosting.jobPosting.isActive.eq(isActive);
    }
    
    // Date range filters
    public static BooleanExpression createdDateBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QJobPosting.jobPosting.createdDate.between(from, to);
        } else if (from != null) {
            return QJobPosting.jobPosting.createdDate.goe(from);
        } else {
            return QJobPosting.jobPosting.createdDate.loe(to);
        }
    }
    
    public static BooleanExpression modifiedDateBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QJobPosting.jobPosting.modifiedDate.between(from, to);
        } else if (from != null) {
            return QJobPosting.jobPosting.modifiedDate.goe(from);
        } else {
            return QJobPosting.jobPosting.modifiedDate.loe(to);
        }
    }
    
    // Salary range filters
    public static BooleanExpression salaryMinBetween(BigDecimal from, BigDecimal to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QJobPosting.jobPosting.salaryMin.between(from, to);
        } else if (from != null) {
            return QJobPosting.jobPosting.salaryMin.goe(from);
        } else {
            return QJobPosting.jobPosting.salaryMin.loe(to);
        }
    }
    
    public static BooleanExpression salaryMaxBetween(BigDecimal from, BigDecimal to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QJobPosting.jobPosting.salaryMax.between(from, to);
        } else if (from != null) {
            return QJobPosting.jobPosting.salaryMax.goe(from);
        } else {
            return QJobPosting.jobPosting.salaryMax.loe(to);
        }
    }
} 