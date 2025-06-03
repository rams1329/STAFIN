package com.HyundaiAutoever.ATS.util;

import com.HyundaiAutoever.ATS.entity.QContactMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

public class ContactMessagePredicate {
    
    // Global search across multiple fields
    public static BooleanExpression globalSearch(String search) {
        if (ObjectUtils.isEmpty(search)) {
            return null;
        }
        
        return QContactMessage.contactMessage.name.containsIgnoreCase(search)
                .or(QContactMessage.contactMessage.email.containsIgnoreCase(search))
                .or(QContactMessage.contactMessage.phoneNumber.containsIgnoreCase(search))
                .or(QContactMessage.contactMessage.message.containsIgnoreCase(search))
                .or(QContactMessage.contactMessage.readBy.containsIgnoreCase(search));
    }
    
    // Individual field searches
    public static BooleanExpression hasName(String name) {
        return ObjectUtils.isEmpty(name) ? null : QContactMessage.contactMessage.name.containsIgnoreCase(name);
    }
    
    public static BooleanExpression hasEmail(String email) {
        return ObjectUtils.isEmpty(email) ? null : QContactMessage.contactMessage.email.containsIgnoreCase(email);
    }
    
    public static BooleanExpression hasMessage(String message) {
        return ObjectUtils.isEmpty(message) ? null : QContactMessage.contactMessage.message.containsIgnoreCase(message);
    }
    
    public static BooleanExpression hasReadByUser(String readByUser) {
        return ObjectUtils.isEmpty(readByUser) ? null : QContactMessage.contactMessage.readBy.containsIgnoreCase(readByUser);
    }
    
    // Status filter
    public static BooleanExpression hasReadStatus(Boolean isRead) {
        return isRead == null ? null : QContactMessage.contactMessage.isRead.eq(isRead);
    }
    
    public static BooleanExpression isUnread() {
        return QContactMessage.contactMessage.isRead.isFalse();
    }
    
    // Date range filters
    public static BooleanExpression createdDateBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QContactMessage.contactMessage.createdAt.between(from, to);
        } else if (from != null) {
            return QContactMessage.contactMessage.createdAt.goe(from);
        } else {
            return QContactMessage.contactMessage.createdAt.loe(to);
        }
    }
    
    public static BooleanExpression readDateBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        
        if (from != null && to != null) {
            return QContactMessage.contactMessage.readAt.between(from, to);
        } else if (from != null) {
            return QContactMessage.contactMessage.readAt.goe(from);
        } else {
            return QContactMessage.contactMessage.readAt.loe(to);
        }
    }
} 