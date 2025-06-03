package com.HyundaiAutoever.ATS.service.impl;

import com.HyundaiAutoever.ATS.dto.ContactMessageDto;
import com.HyundaiAutoever.ATS.entity.ContactMessage;
import com.HyundaiAutoever.ATS.exception.ResourceNotFoundException;
import com.HyundaiAutoever.ATS.repository.ContactMessageRepository;
import com.HyundaiAutoever.ATS.service.ContactMessageService;
import com.HyundaiAutoever.ATS.util.ContactMessagePredicate;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Override
    @Transactional
    public ContactMessageDto saveContactMessage(ContactMessageDto contactMessageDto) {
        ContactMessage contactMessage = ContactMessage.builder()
                .name(contactMessageDto.getName())
                .email(contactMessageDto.getEmail())
                .phoneNumber(contactMessageDto.getPhoneNumber())
                .message(contactMessageDto.getMessage())
                .isRead(false)
                .build();
        
        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
        return mapToDto(savedMessage);
    }

    @Override
    public List<ContactMessageDto> getAllContactMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactMessageDto> getAllContactMessagesWithFilters(
            String search, String name, String email, String message,
            Boolean isRead, String readByUser,
            LocalDateTime createdDateFrom, LocalDateTime createdDateTo,
            LocalDateTime readDateFrom, LocalDateTime readDateTo,
            Pageable pageable) {
        
        BooleanBuilder predicate = new BooleanBuilder();
        
        // Global search - takes precedence over individual field searches
        if (search != null && !search.trim().isEmpty()) {
            predicate.and(ContactMessagePredicate.globalSearch(search));
        } else {
            // Individual field searches (only if no global search)
            if (name != null) {
                predicate.and(ContactMessagePredicate.hasName(name));
            }
            
            if (email != null) {
                predicate.and(ContactMessagePredicate.hasEmail(email));
            }
            
            if (message != null) {
                predicate.and(ContactMessagePredicate.hasMessage(message));
            }
        }
        
        // Status filter
        if (isRead != null) {
            predicate.and(ContactMessagePredicate.hasReadStatus(isRead));
        }
        
        // Read by user filter
        if (readByUser != null) {
            predicate.and(ContactMessagePredicate.hasReadByUser(readByUser));
        }
        
        // Date range filters
        if (createdDateFrom != null || createdDateTo != null) {
            predicate.and(ContactMessagePredicate.createdDateBetween(createdDateFrom, createdDateTo));
        }
        
        if (readDateFrom != null || readDateTo != null) {
            predicate.and(ContactMessagePredicate.readDateBetween(readDateFrom, readDateTo));
        }
        
        return contactMessageRepository.findAll(predicate, pageable)
                .map(this::mapToDto);
    }

    @Override
    public List<ContactMessageDto> getUnreadContactMessages() {
        return contactMessageRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContactMessageDto getContactMessageById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        
        return mapToDto(message);
    }

    @Override
    @Transactional
    public ContactMessageDto markAsRead(Long id, String username) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        
        if (!message.isRead()) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            message.setReadBy(username);
            contactMessageRepository.save(message);
        }
        
        return mapToDto(message);
    }

    @Override
    public long countUnreadMessages() {
        return contactMessageRepository.countByIsReadFalse();
    }

    @Override
    @Transactional
    public void deleteContactMessage(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        
        contactMessageRepository.delete(message);
    }
    
    private ContactMessageDto mapToDto(ContactMessage message) {
        return ContactMessageDto.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .phoneNumber(message.getPhoneNumber())
                .message(message.getMessage())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .readBy(message.getReadBy())
                .build();
    }
} 