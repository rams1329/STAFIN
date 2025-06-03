package com.HyundaiAutoever.ATS.controller;

import com.HyundaiAutoever.ATS.dto.ApiResponse;
import com.HyundaiAutoever.ATS.dto.ContactMessageDto;
import com.HyundaiAutoever.ATS.security.UserPrincipal;
import com.HyundaiAutoever.ATS.service.ContactMessageService;
import com.HyundaiAutoever.ATS.service.UserService;
import com.HyundaiAutoever.ATS.util.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactMessageController {

    private final ContactMessageService contactMessageService;
    private final EmailService emailService;
    private final UserService userService;
    
    @Value("${app.admin.notification.email:admin@example.com}")
    private String adminNotificationEmail;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageDto>> submitContactForm(
            @Valid @RequestBody ContactMessageDto contactMessageDto) {
        try {
            ContactMessageDto savedMessage = contactMessageService.saveContactMessage(contactMessageDto);
            
            // Send confirmation email to the person who submitted the form
            try {
                emailService.sendContactConfirmationEmail(contactMessageDto.getEmail(), contactMessageDto.getName());
            } catch (Exception e) {
                log.error("Failed to send confirmation email to user", e);
                // Don't fail the request if email sending fails
            }
            
            // Send notification email to admin
            try {
                emailService.sendNewContactMessageNotification(
                    adminNotificationEmail,
                    contactMessageDto.getName(),
                    contactMessageDto.getEmail(),
                    contactMessageDto.getMessage()
                );
            } catch (Exception e) {
                log.error("Failed to send notification email to admin", e);
                // Don't fail the request if email sending fails
            }
            
            return ResponseEntity.ok(ApiResponse.success("Message sent successfully", savedMessage));
        } catch (Exception e) {
            log.error("Error submitting contact form", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ContactMessageDto>>> getAllMessages(
            // Search parameters
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String message,
            
            // Filter parameters
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String readByUser,
            
            // Date range filters
            @RequestParam(required = false) LocalDateTime createdDateFrom,
            @RequestParam(required = false) LocalDateTime createdDateTo,
            @RequestParam(required = false) LocalDateTime readDateFrom,
            @RequestParam(required = false) LocalDateTime readDateTo,
            
            // Pagination and sorting
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ContactMessageDto> messages = contactMessageService.getAllContactMessagesWithFilters(
            search, name, email, message, isRead, readByUser,
            createdDateFrom, createdDateTo, readDateFrom, readDateTo, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @GetMapping("/messages/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ContactMessageDto>>> getUnreadMessages() {
        List<ContactMessageDto> messages = contactMessageService.getUnreadContactMessages();
        return ResponseEntity.ok(ApiResponse.success("Unread messages retrieved successfully", messages));
    }

    @GetMapping("/messages/count-unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnreadMessages() {
        long count = contactMessageService.countUnreadMessages();
        Map<String, Long> resultMap = Map.of("count", count);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", resultMap));
    }

    @GetMapping("/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContactMessageDto>> getMessageById(@PathVariable Long id) {
        try {
            ContactMessageDto message = contactMessageService.getContactMessageById(id);
            return ResponseEntity.ok(ApiResponse.success("Message retrieved successfully", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/messages/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContactMessageDto>> markAsRead(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            ContactMessageDto message = contactMessageService.markAsRead(id, username);
            return ResponseEntity.ok(ApiResponse.success("Message marked as read", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        try {
            contactMessageService.deleteContactMessage(id);
            log.info("Contact message with id {} deleted successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting contact message with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 