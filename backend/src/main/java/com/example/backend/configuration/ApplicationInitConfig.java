package com.example.backend.configuration;

import com.example.backend.constant.PredefinedPermission;
import com.example.backend.constant.PredefinedRole;
import com.example.backend.entity.Permission;
import com.example.backend.entity.User;
import com.example.backend.entity.Role;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PermissionRepository permissionRepository) {
        log.info("Initializing application.....");

        return args -> {
            // --- Step 1: Insert predefined permissions if missing ---
            log.info("Seeding predefined permissions...");
            Arrays.stream(PredefinedPermission.values()).forEach(p -> {
                if (!permissionRepository.existsById(p.getName())) {
                    Permission permission = Permission.builder()
                            .name(p.getName())
                            .description(p.getDescription())
                            .build();
                    permissionRepository.save(permission);
                    log.info("Inserted permission: {}", p.getName());
                }
            });

            // --- Step 2: Create USER role if missing ---
            Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE)
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .name(PredefinedRole.USER_ROLE)
                                .description("User role")
                                .build();
                        return roleRepository.save(newRole);
                    });

            // --- Step 3: Create ADMIN role with all permissions ---
            Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                    .orElseGet(() -> {
                        List<Permission> allPermissions = permissionRepository.findAll();
                        Role newRole = Role.builder()
                                .name(PredefinedRole.ADMIN_ROLE)
                                .description("Admin role")
                                .permissions(new HashSet<>(allPermissions))
                                .build();
                        return roleRepository.save(newRole);
                    });

            // --- Step 4: Create admin user if missing ---
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("Admin user created with default password: '{}'. Please change it!", ADMIN_PASSWORD);
            }

            log.info("Application initialization completed ✅");
        };
    }
}