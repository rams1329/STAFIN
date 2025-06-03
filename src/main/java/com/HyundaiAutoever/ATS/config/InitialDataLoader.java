package com.HyundaiAutoever.ATS.config;

import com.HyundaiAutoever.ATS.entity.Role;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.repository.RoleRepository;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

// Disabled in favor of DataInitializer which provides more comprehensive initialization
// @Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public InitialDataLoader(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // Create roles
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "Administrator role");
        Role userRole = createRoleIfNotFound("ROLE_USER", "Standard user role");

        // Create default admin user
        if (!userRepository.existsByEmail("admin@example.com")) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            
            User adminUser = User.builder()
                    .name("Administrator")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .active(true)
                    .firstLogin(false)
                    .roles(adminRoles)
                    .build();
            
            userRepository.save(adminUser);
            
            System.out.println("===== DEFAULT ADMIN USER CREATED =====");
            System.out.println("Email: admin@example.com");
            System.out.println("Password: admin123");
            System.out.println("======================================");
        }

        alreadySetup = true;
    }

    @Transactional
    Role createRoleIfNotFound(String name, String description) {
        Role role = roleRepository.findByName(name).orElse(null);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
        return role;
    }
} 