package com.HyundaiAutoever.ATS.service;

import com.HyundaiAutoever.ATS.dto.ContactMessageDto;
import com.HyundaiAutoever.ATS.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ContactMessageService {
    
    /**
     * Save a new contact message
     */
    ContactMessageDto saveContactMessage(ContactMessageDto contactMessageDto);
    
    /**
     * Get all contact messages
     */
    List<ContactMessageDto> getAllContactMessages();
    
    /**
     * Get all unread contact messages
     */
    List<ContactMessageDto> getUnreadContactMessages();
    
    /**
     * Get a contact message by ID
     */
    ContactMessageDto getContactMessageById(Long id);
    
    /**
     * Mark a contact message as read
     */
    ContactMessageDto markAsRead(Long id, String username);
    
    /**
     * Count unread messages
     */
    long countUnreadMessages();

    /**
     * Delete a contact message by ID
     */
    void deleteContactMessage(Long id);

    // Enhanced method with comprehensive filters
    Page<ContactMessageDto> getAllContactMessagesWithFilters(
        String search, String name, String email, String message,
        Boolean isRead, String readByUser,
        LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
        LocalDateTime readDateFrom, LocalDateTime readDateTo,
        Pageable pageable);
} 