package com.HyundaiAutoever.ATS.config;

import com.HyundaiAutoever.ATS.entity.MenuMaster;
import com.HyundaiAutoever.ATS.entity.MenuTransaction;
import com.HyundaiAutoever.ATS.entity.Role;
import com.HyundaiAutoever.ATS.entity.User;
import com.HyundaiAutoever.ATS.repository.MenuMasterRepository;
import com.HyundaiAutoever.ATS.repository.MenuTransactionRepository;
import com.HyundaiAutoever.ATS.repository.RoleRepository;
import com.HyundaiAutoever.ATS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuMasterRepository menuMasterRepository;
    private final MenuTransactionRepository menuTransactionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
        initMenus();
    }

    private void initializeRoles() {
        // Create ROLE_ADMIN if it doesn't exist
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator role with full access")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        }

        // Create ROLE_USER if it doesn't exist
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Regular user role")
                    .build();
            roleRepository.save(userRole);
            log.info("Created ROLE_USER");
        }
    }

    private void initializeAdminUser() {
        // Create default admin user if no admin exists
        String adminEmail = "admin@example.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            User adminUser = User.builder()
                    .name("Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .active(true)
                    .firstLogin(false)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(adminUser);
            log.info("Created default admin user: {} / admin123", adminEmail);
        }
    }

    private void initMenus() {
        if (menuMasterRepository.count() == 0) {
            // Create dashboard menu
            MenuMaster dashboard = MenuMaster.builder()
                    .name("Dashboard")
                    .description("Application dashboard")
                    .icon("dashboard")
                    .url("/dashboard")
                    .displayOrder(1)
                    .active(true)
                    .build();
            menuMasterRepository.save(dashboard);

            // Create job management menu (admin only)
            MenuMaster jobManagement = MenuMaster.builder()
                    .name("Job Management")
                    .description("Manage job postings")
                    .icon("work")
                    .url("/jobs")
                    .displayOrder(2)
                    .active(true)
                    .build();
            menuMasterRepository.save(jobManagement);

            // Create create job menu (admin only)
            MenuMaster createJob = MenuMaster.builder()
                    .name("Create Job")
                    .description("Create new job posting")
                    .icon("add_circle")
                    .url("/jobs/new")
                    .displayOrder(3)
                    .active(true)
                    .build();
            menuMasterRepository.save(createJob);

            // Create applications menu (admin only)
            MenuMaster applications = MenuMaster.builder()
                    .name("Applications")
                    .description("Manage job applications")
                    .icon("assignment")
                    .url("/applications")
                    .displayOrder(4)
                    .active(true)
                    .build();
            menuMasterRepository.save(applications);

            // Create settings menu (admin only)
            MenuMaster settings = MenuMaster.builder()
                    .name("Settings")
                    .description("System settings")
                    .icon("settings")
                    .url("/settings")
                    .displayOrder(5)
                    .active(true)
                    .build();
            menuMasterRepository.save(settings);

            // Create user management menu (admin only)
            MenuMaster userManagement = MenuMaster.builder()
                    .name("User Management")
                    .description("Manage users")
                    .icon("people")
                    .url("/users")
                    .displayOrder(6)
                    .active(true)
                    .build();
            menuMasterRepository.save(userManagement);

            // Create role management menu (admin only)
            MenuMaster roleManagement = MenuMaster.builder()
                    .name("Role Management")
                    .description("Manage roles")
                    .icon("security")
                    .url("/roles")
                    .displayOrder(7)
                    .active(true)
                    .build();
            menuMasterRepository.save(roleManagement);

            // Create menu management menu (admin only)
            MenuMaster menuManagement = MenuMaster.builder()
                    .name("Menu Management")
                    .description("Manage menus")
                    .icon("menu")
                    .url("/menu-management")
                    .displayOrder(8)
                    .active(true)
                    .build();
            menuMasterRepository.save(menuManagement);

            // Create job search menu (user only)
            MenuMaster jobSearch = MenuMaster.builder()
                    .name("Job Search")
                    .description("Search and view available jobs")
                    .icon("search")
                    .url("/user-dashboard")
                    .displayOrder(9)
                    .active(true)
                    .build();
            menuMasterRepository.save(jobSearch);

            // Assign menus to admin role
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            MenuMaster[] adminMenus = {dashboard, jobManagement, createJob, applications, settings, userManagement, roleManagement, menuManagement};
            
            for (MenuMaster menu : adminMenus) {
                MenuTransaction transaction = MenuTransaction.builder()
                        .menu(menu)
                        .role(adminRole)
                        .canView(true)
                        .canAdd(true)
                        .canEdit(true)
                        .canDelete(true)
                        .build();
                menuTransactionRepository.save(transaction);
            }

            // Assign menus to user role
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
            MenuMaster[] userMenus = {jobSearch};
            
            for (MenuMaster menu : userMenus) {
                MenuTransaction transaction = MenuTransaction.builder()
                        .menu(menu)
                        .role(userRole)
                        .canView(true)
                        .canAdd(false)
                        .canEdit(false)
                        .canDelete(false)
                        .build();
                menuTransactionRepository.save(transaction);
            }

            log.info("Initialized default menus");
        }
    }
} 