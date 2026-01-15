package com.example.backend.integration;

import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.request.OrderSelectedItemsRequest;
import com.example.backend.dto.request.OrderBuyNowRequest;
import java.util.ArrayList;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.service.UserService;
import com.example.backend.service.OrderService;
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
class SecurityIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        //clean before each test
        userRepository.deleteAll();
        cartRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    //================= IDOR PREVENTION TESTS =================

    @Test
    void updateUser_idorPrevention_shouldBlockUnauthorizedAccess() {
        //Step 1: Tạo 2 users
        UserCreationRequest userA = UserCreationRequest.builder()
                .username("userA")
                .password("password123")
                .fullname("User A")
                .email("userA@sis.hust.edu.vn")
                .build();

        UserCreationRequest userB = UserCreationRequest.builder()
                .username("userB")
                .password("password123")
                .fullname("User B")
                .email("userB@sis.hust.edu.vn")
                .build();

        UserResponse responseA = userService.createUser(userA);
        UserResponse responseB = userService.createUser(userB);

        //Step 2: userA cố update userB
        Authentication authA = new UsernamePasswordAuthenticationToken(
                "userA",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authA);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullname("Hacked Name")
                .build();

        //Step 3: Should throw UNAUTHORIZED
        AppException exception = assertThrows(AppException.class, () -> {
            userService.updateUser(responseB.getUserId(), updateRequest);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());

        //Step 4: Verify userB không bị update
        SecurityContextHolder.clearContext();
        Optional<User> userBCheck = userRepository.findById(responseB.getUserId());
        assertTrue(userBCheck.isPresent());
        assertEquals("User B", userBCheck.get().getFullname()); //vẫn giữ tên cũ

        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteUser_idorPrevention_shouldBlockUnauthorizedAccess() {
        //Tạo user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("victim")
                .password("password123")
                .fullname("Victim User")
                .email("victim@sis.hust.edu.vn")
                .build();

        UserResponse victim = userService.createUser(request);

        //Regular user cố delete user khác
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "attacker",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        //Should throw UNAUTHORIZED vì deleteUser require admin
        AppException exception = assertThrows(AppException.class, () -> {
            userService.deleteUser(victim.getUserId());
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());

        //Verify user vẫn tồn tại
        SecurityContextHolder.clearContext();
        Optional<User> victimCheck = userRepository.findById(victim.getUserId());
        assertTrue(victimCheck.isPresent());

        SecurityContextHolder.clearContext();
    }

    //================= PRIVILEGE ESCALATION TESTS =================

    @Test
    void adminEndpoint_nonAdminUser_shouldBeForbidden() {
        //Tạo regular user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("regularuser")
                .password("password123")
                .fullname("Regular User")
                .email("regular@sis.hust.edu.vn")
                .build();

        userService.createUser(request);

        //Mock regular user authentication
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "regularuser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        //Cố access admin-only method (getAllUser requires admin)
        AppException exception = assertThrows(AppException.class, () -> {
            userService.getAllUser();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        SecurityContextHolder.clearContext();
    }

    @Test
    void banUser_nonAdminUser_shouldBeForbidden() {
        //Tạo user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("targetuser")
                .password("password123")
                .fullname("Target User")
                .email("target@sis.hust.edu.vn")
                .build();

        UserResponse target = userService.createUser(request);

        //Regular user cố ban user khác
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "attacker",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        //Should fail vì banUser require admin
        AppException exception = assertThrows(AppException.class, () -> {
            userService.banUser(target.getUserId());
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());

        //Verify user không bị ban
        SecurityContextHolder.clearContext();
        Optional<User> targetCheck = userRepository.findById(target.getUserId());
        assertTrue(targetCheck.isPresent());
        assertEquals("ACTIVE", targetCheck.get().getStatus());

        SecurityContextHolder.clearContext();
    }

    //================= BANNED USER RESTRICTION TESTS =================

    @Test
    void checkout_bannedUser_shouldBlockOrder() {
        //Step 1: Tạo user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("normaluser")
                .password("password123")
                .fullname("Normal User")
                .email("normal@sis.hust.edu.vn")
                .build();

        UserResponse user = userService.createUser(request);

        //Step 2: Admin bans user
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        userService.banUser(user.getUserId());
        SecurityContextHolder.clearContext();

        //Step 3: Banned user tries checkout
        Authentication bannedUserAuth = new UsernamePasswordAuthenticationToken(
                "normaluser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(bannedUserAuth);

        //Should throw USER_BANNED error
        OrderSelectedItemsRequest checkoutRequest = new OrderSelectedItemsRequest();
        checkoutRequest.setProductIds(new ArrayList<>());
        
        AppException exception = assertThrows(AppException.class, () -> {
            orderService.checkoutSelectedItems(checkoutRequest);
        });

        assertEquals(ErrorCode.USER_BANNED, exception.getErrorCode());
        SecurityContextHolder.clearContext();
    }

    @Test
    void buyNow_bannedUser_shouldBlockOrder() {
        //Tạo user
        UserCreationRequest request = UserCreationRequest.builder()
                .username("buyer")
                .password("password123")
                .fullname("Buyer User")
                .email("buyer@sis.hust.edu.vn")
                .build();

        UserResponse user = userService.createUser(request);

        //Admin bans user
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        userService.banUser(user.getUserId());
        SecurityContextHolder.clearContext();

        //Banned user tries buy now
        Authentication bannedUserAuth = new UsernamePasswordAuthenticationToken(
                "buyer",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(bannedUserAuth);

        //Should throw USER_BANNED
        OrderBuyNowRequest buyNowRequest = OrderBuyNowRequest.builder()
                .productId("product-1")
                .quantity(1)
                .build();
        
        AppException exception = assertThrows(AppException.class, () -> {
            orderService.checkoutBuyNow(buyNowRequest);
        });

        assertEquals(ErrorCode.USER_BANNED, exception.getErrorCode());
        SecurityContextHolder.clearContext();
    }
}
