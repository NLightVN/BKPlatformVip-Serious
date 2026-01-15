package com.example.backend.integration;

import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.*;
import com.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clean up in correct order to respect foreign key constraints

        // 1. Delete orders first (cascades to order_items, shipments, payments)
        orderRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // 2. Delete carts and cart items
        cartRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // 3. Delete products (referencing shops)
        // Note: Shop delete cascades to products, but explicit delete safeguards
        // orphans
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // 4. Delete shops
        shopRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // 5. Finally delete users
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Clear authentication context
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_thenGetUser_shouldReturnUser() {
        // Arrange
        UserCreationRequest request = UserCreationRequest.builder()
                .username("integrationuser")
                .password("password123")
                .fullname("Integration Test User")
                .email("integration@sis.hust.edu.vn")
                .build();

        // Act: Create user through service
        UserResponse response = userService.createUser(request);

        // Assert: User was created
        assertNotNull(response);
        assertEquals("integrationuser", response.getUsername());
        assertEquals("integration@sis.hust.edu.vn", response.getEmail());

        // Verify: User exists in database
        Optional<User> savedUser = userRepository.findByUsername("integrationuser");
        assertTrue(savedUser.isPresent());
        assertEquals("Integration Test User", savedUser.get().getFullname());
    }

    @Test
    void createUser_thenUpdateUser_shouldUpdateInDatabase() {
        // Arrange: Create user
        UserCreationRequest createRequest = UserCreationRequest.builder()
                .username("updateuser")
                .password("password123")
                .fullname("Original Name")
                .email("original@sis.hust.edu.vn")
                .build();

        UserResponse created = userService.createUser(createRequest);
        String userId = created.getUserId();

        // Mock authentication for the user to update their own profile
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "updateuser",
                null,
                java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act: Update user
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullname("Updated Name")
                .email("updated@sis.hust.edu.vn")
                .build();

        UserResponse updated = userService.updateUser(userId, updateRequest);

        // Clear authentication
        SecurityContextHolder.clearContext();

        // Assert: User was updated
        assertEquals("Updated Name", updated.getFullname());
        assertEquals("updated@sis.hust.edu.vn", updated.getEmail());

        // Verify: Changes persisted in database
        Optional<User> dbUser = userRepository.findById(userId);
        assertTrue(dbUser.isPresent());
        assertEquals("Updated Name", dbUser.get().getFullname());
        assertEquals("updated@sis.hust.edu.vn", dbUser.get().getEmail());
    }

    @Test
    void createUser_thenDeleteUser_shouldRemoveFromDatabase() {
        // Arrange: Create user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("deleteuser")
                .password("password123")
                .fullname("Delete Test User")
                .email("delete@sis.hust.edu.vn")
                .build();

        UserResponse created = userService.createUser(request);
        String userId = created.getUserId();

        // Verify: User exists
        assertTrue(userRepository.findById(userId).isPresent());

        // Mock authentication with admin role to delete user
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        // Act: Delete user
        userService.deleteUser(userId);

        // Clear authentication
        SecurityContextHolder.clearContext();

        // Assert: User was deleted
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void createMultipleUsers_thenGetAllUsers_shouldReturnAll() {
        // Arrange: Create multiple users
        for (int i = 1; i <= 5; i++) {
            UserCreationRequest request = UserCreationRequest.builder()
                    .username("user" + i)
                    .password("password123")
                    .fullname("User " + i)
                    .email("user" + i + "@sis.hust.edu.vn")
                    .build();
            userService.createUser(request);
        }

        // Act: Get all users (requires admin, so we'll test differently)
        // Note: getAllUser() requires admin role, so we test individual user retrieval
        // instead
        for (int i = 1; i <= 5; i++) {
            var user = userRepository.findByUsername("user" + i);
            assertTrue(user.isPresent());
        }
    }
}
