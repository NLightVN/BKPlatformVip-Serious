package com.example.backend.configuration;

import com.example.backend.constant.PredefinedRole;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

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
                                        RoleRepository roleRepository) {

        log.info("Initializing application.....");

        return args -> {

            // --- Step 1: Create USER role if missing ---
            Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(PredefinedRole.USER_ROLE)
                                    .description("User role")
                                    .build()
                    ));

            // --- Step 2: Create ADMIN role if missing ---
            Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(PredefinedRole.ADMIN_ROLE)
                                    .description("Admin role")
                                    .build()
                    ));

            // --- Step 3: Create admin user if missing ---
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User admin = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(admin);

                log.warn("Admin user created with default password: '{}'. Please change it!", ADMIN_PASSWORD);
            }

            log.info("Application initialization completed ✅");
        };
    }
}
