package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private RoleMapper roleMapper;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        // Inject mappers using reflection
        ReflectionTestUtils.setField(Objects.requireNonNull(userMapper), "addressMapper", addressMapper);
        ReflectionTestUtils.setField(Objects.requireNonNull(userMapper), "roleMapper", roleMapper);
        // vì khi mock userMapper thì trong nó sẽ ko có sắn mock của addressMapper và roleMapper nên cần reflection
    }

    @Test
    void toUser_shouldMapCorrectly() {
        //setup
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("password123")
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .address(AddressDTO.builder()
                        .name("Test Address")
                        .phone("0123456789")
                        .addressDetail("123 Street")
                        .wardCode("001")
                        .build())
                .build();

        AddressBook addressBook = AddressBook.builder()
                .name("Test Address")
                .phone("0123456789")
                .addressDetail("123 Street")
                .build();

        when(addressMapper.toEntity(request.getAddress())).thenReturn(addressBook);

        //chay
        User user = userMapper.toUser(request);

        //check
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getFullname());
        assertEquals("test@sis.hust.edu.vn", user.getEmail());
        // Roles should be ignored
        assertNull(user.getRoles());
    }

    @Test
    void toUser_shouldIgnoreRoles() {
        //setup
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("password123")
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .build();

        //chay
        User user = userMapper.toUser(request);

        //check
        assertNull(user.getRoles());
    }

    @Test
    void toUser_nullRequest_returnsNull() {
        //chay
        User user = userMapper.toUser(null);

        //check
        assertNull(user);
    }

    @Test
    void toUserResponse_shouldMapCorrectly() {
        //setup
        Role role1 = Role.builder()
                .name("USER")
                .description("User role")
                .build();

        Role role2 = Role.builder()
                .name("ADMIN")
                .description("Admin role")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        AddressBook address = AddressBook.builder()
                .name("User Address")
                .phone("0123456789")
                .addressDetail("123 Street")
                .build();

        User user = User.builder()
                .userId("user-1")
                .username("testuser")
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .roles(roles)
                .createdDate(LocalDateTime.now())
                .address(address)
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .name("User Address")
                .phone("0123456789")
                .addressDetail("123 Street")
                .wardCode("001")
                .build();

        RoleResponse roleResponse1 = RoleResponse.builder()
                .name("USER")
                .description("User role")
                .build();

        RoleResponse roleResponse2 = RoleResponse.builder()
                .name("ADMIN")
                .description("Admin role")
                .build();

        when(addressMapper.toDto(address)).thenReturn(addressDTO);
        when(roleMapper.toRoleResponse(role1)).thenReturn(roleResponse1);
        when(roleMapper.toRoleResponse(role2)).thenReturn(roleResponse2);

        //chay
        UserResponse response = userMapper.toUserResponse(user);

        //check
        assertNotNull(response);
        assertEquals("user-1", response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getFullname());
        assertEquals("test@sis.hust.edu.vn", response.getEmail());
        assertNotNull(response.getRoles());
        assertEquals(2, response.getRoles().size());
        assertNotNull(response.getAddress());
    }

    @Test
    void toUserResponse_nullUser_returnsNull() {
        //chay
        UserResponse response = userMapper.toUserResponse(null);

        //check
        assertNull(response);
    }

    @Test
    void updateUser_shouldUpdateNonNullFields() {
        //setup
        User existingUser = User.builder()
                .userId("user-1")
                .username("olduser")
                .fullname("Old Name")
                .email("old@sis.hust.edu.vn")
                .password("oldpassword")
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("New Name")
                .email("new@sis.hust.edu.vn")
                .password("newpassword")
                .build();

        //chay
        userMapper.updateUser(existingUser, request);

        //check
        assertEquals("New Name", existingUser.getFullname());
        assertEquals("new@sis.hust.edu.vn", existingUser.getEmail());
        assertEquals("newpassword", existingUser.getPassword());
        // Username should remain unchanged
        assertEquals("olduser", existingUser.getUsername());
    }

    @Test
    void updateUser_shouldIgnoreRoles() {
        //setup
        Set<Role> originalRoles = new HashSet<>();
        originalRoles.add(Role.builder().name("USER").build());

        User existingUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .roles(originalRoles)
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("Updated Name")
                .roles(java.util.List.of("ADMIN"))
                .build();

        //chay
        userMapper.updateUser(existingUser, request);

        //check
        // Roles should be ignored (handled separately in service)
        assertNotNull(existingUser.getRoles());
        assertEquals(1, existingUser.getRoles().size());
    }

    @Test
    void updateUser_shouldMapAddress() {
        //setup
        AddressBook existingAddress = AddressBook.builder()
                .name("Old Address")
                .phone("0123456789")
                .addressDetail("Old Street")
                .build();

        User existingUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .address(existingAddress)
                .build();

        AddressDTO newAddressDTO = AddressDTO.builder()
                .name("New Address")
                .phone("0987654321")
                .addressDetail("New Street")
                .wardCode("002")
                .build();

        AddressBook newAddress = AddressBook.builder()
                .name("New Address")
                .phone("0987654321")
                .addressDetail("New Street")
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .address(newAddressDTO)
                .build();

        when(addressMapper.toEntity(newAddressDTO)).thenReturn(newAddress);

        //chay
        userMapper.updateUser(existingUser, request);

        //check
        verify(addressMapper, times(1)).toEntity(newAddressDTO);
        // Note: The actual update behavior depends on MapStruct implementation
    }

    @Test
    void updateUser_withNullValues_shouldIgnoreNullFields() {
        //setup
        User existingUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .fullname("Original Name")
                .email("original@sis.hust.edu.vn")
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("Updated Name")
                .email(null) // null value
                .password(null) // null value
                .build();

        //chay
        userMapper.updateUser(existingUser, request);

        //check
        assertEquals("Updated Name", existingUser.getFullname());
        // With NullValuePropertyMappingStrategy.IGNORE, null fields should be ignored
        // Original email should remain
        assertEquals("original@sis.hust.edu.vn", existingUser.getEmail());
    }

    @Test
    void toUserResponse_withNullRoles_shouldHandleGracefully() {
        //setup
        User user = User.builder()
                .userId("user-1")
                .username("testuser")
                .roles(null)
                .build();

        //chay
        UserResponse response = userMapper.toUserResponse(user);

        //check
        assertNotNull(response);
        // Roles might be null or empty set depending on MapStruct implementation
    }

    @Test
    void toUserResponse_withNullAddress_shouldHandleGracefully() {
        //setup
        User user = User.builder()
                .userId("user-1")
                .username("testuser")
                .address(null)
                .build();

        //chay
        UserResponse response = userMapper.toUserResponse(user);

        //check
        assertNotNull(response);
        // Address might be null depending on MapStruct implementation
    }
}
