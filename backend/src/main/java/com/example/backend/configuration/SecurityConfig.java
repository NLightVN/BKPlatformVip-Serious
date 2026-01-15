package com.example.backend.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/locations/**",      // Địa điểm public
            "/products/**",       // Product public (xem)
            "/shops/**",          // Shop public (xem)
            "/categories/**"      // Category public
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> request
                // 1. Cho phép các API Auth & Tạo user (POST)
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/shipping/**").permitAll()
                // 2. Chỉ cho phép XEM (GET) các thông tin sản phẩm, shop, v.v. public
                .requestMatchers(HttpMethod.GET, "/locations/**", "/products/**", "/shops/**", "/categories/**").permitAll()

                // 3. Các hành động thay đổi dữ liệu (POST/PUT/DELETE) trên các tài nguyên trên phải login
                // (Hoặc bạn có thể dùng @PreAuthorize ở Controller)

                // 4. Các request còn lại phải được xác thực
                .anyRequest().authenticated()
        );
        httpSecurity.cors(Customizer.withDefaults());
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // giữ trống
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
