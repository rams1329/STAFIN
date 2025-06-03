package com.HyundaiAutoever.ATS.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpEmail(String to, String otp, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);
            
            String htmlContent = templateEngine.process("otp-email", context);
            
            helper.setTo(to);
            helper.setSubject("Your ATS Account One-Time Password");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void sendSimpleOtpEmail(String to, String otp, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String content = String.format(
                    "Hello %s,<br><br>" +
                    "Your one-time password (OTP) for the ATS application is: <b>%s</b><br><br>" +
                    "Please use this OTP to log in and reset your password.<br><br>" +
                    "This OTP is valid for a single use only.<br><br>" +
                    "Regards,<br>" +
                    "ATS Team", name, otp);
            
            helper.setTo(to);
            helper.setSubject("Your ATS Account One-Time Password");
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("Simple OTP email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send simple OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send simple OTP email", e);
        }
    }
    
    /**
     * Send a confirmation email to a user who submitted a contact message
     */
    public void sendContactConfirmationEmail(String to, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String content = String.format(
                    "Hello %s,<br><br>" +
                    "Thank you for contacting us. We have received your message and will get back to you shortly.<br><br>" +
                    "Regards,<br>" +
                    "ATS Team", name);
            
            helper.setTo(to);
            helper.setSubject("Thank You for Contacting Us");
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("Contact confirmation email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send contact confirmation email to: {}", to, e);
            throw new RuntimeException("Failed to send contact confirmation email", e);
        }
    }
    
    /**
     * Send a notification to admins about a new contact message
     */
    public void sendNewContactMessageNotification(String to, String contactName, String contactEmail, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            String content = String.format(
                    "Hello Admin,<br><br>" +
                    "A new contact message has been received from %s (%s).<br><br>" +
                    "<b>Message:</b><br>%s<br><br>" +
                    "Please log in to the admin dashboard to respond.<br><br>" +
                    "Regards,<br>" +
                    "ATS System", contactName, contactEmail, message);
            
            helper.setTo(to);
            helper.setSubject("New Contact Message Received");
            helper.setText(content, true);
            
            mailSender.send(mimeMessage);
            log.info("New contact message notification sent to admin: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send contact message notification to: {}", to, e);
            throw new RuntimeException("Failed to send contact message notification", e);
        }
    }
} 