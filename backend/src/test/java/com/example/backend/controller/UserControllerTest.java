package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    // ================= CREATE USER =================
    @Test
//    @WithMockUser(username = "test", roles = {"USER"})
    void createUser_success() throws Exception {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("12345678")
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .address(AddressDTO.builder()
                        .name("Test")
                        .phone("0123456789")
                        .addressDetail("HN")
                        .wardCode("001")
                        .build())
                .build();

        UserResponse response = UserResponse.builder()
                .userId("uuid-1")
                .username("testuser")
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .createdDate(LocalDateTime.now())
                .build();

        when(userService.createUser(any()))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("testuser"));
    }
    // ================= CREATE USER - VALIDATION =================
    @Test
    void createUser_invalidPassword() throws Exception {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("123") // < 8 ký tự
                .fullname("Test User")
                .email("test@sis.hust.edu.vn")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 8 characters"));
    }

    @Test
    void createUser_invalidEmail() throws Exception {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("12345678")
                .fullname("Test User")
                .email("test@gmail.com") // invalid
                .build();

        // Mock behavior: khi createUser với email không hợp lệ → ném AppException
        when(userService.createUser(any(UserCreationRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_EMAIL));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EMAIL.getMessage()));
    }

    @Test
    void createUser_existedEmail() throws Exception {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("testuser")
                .password("12345678")
                .fullname("Test User")
                .email("Huy.NQ235947@sis.hust.edu.vn") // existed
                .build();

        // Mock behavior: khi createUser với email không hợp lệ → ném AppException
        when(userService.createUser(any(UserCreationRequest.class)))
                .thenThrow(new AppException(ErrorCode.EMAIL_EXISTED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_EXISTED.getMessage()));
    }
    // ================= GET ALL USERS =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_success() throws Exception {
        when(userService.getAllUser())
                .thenReturn(List.of(
                        UserResponse.builder().username("u1").build(),
                        UserResponse.builder().username("u2").build()
                ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllUsers_nonAdmin() throws Exception {
        // Mock service ném AppException khi user không phải admin
        when(userService.getAllUser())
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));


        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden()) // 403
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }

    // ================= GET USER BY ID =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getUserById_success() throws Exception {
        when(userService.getUserById("1"))
                .thenReturn(UserResponse.builder()
                        .userId("1")
                        .username("testuser")
                        .build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getUserById_notFound() throws Exception {
        when(userService.getUserById("999"))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User does not exist"));
    }



    // ================= UPDATE USER =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateUser_success_asAdmin() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("New Name")
                .email("new@sis.hust.edu.vn")
                .build();

        when(userService.updateUser(Mockito.eq("1"), any()))
                .thenReturn(UserResponse.builder()
                        .userId("1")
                        .fullname("New Name")
                        .build());

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullname").value("New Name"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void updateUser_asSelf_success() throws Exception {
        // Dữ liệu update
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("Nguyen Quang Huy")   // Tên mới
                .email("huynew@sis.hust.edu.vn") // Email mới
                .build();

        // Mock service trả về response giống như update thành công
        when(userService.updateUser(Mockito.eq("user1"), any()))
                .thenReturn(UserResponse.builder()
                        .userId("user1")
                        .username("user1")
                        .fullname("Nguyen Quang Huy")
                        .email("huynew@sis.hust.edu.vn")
                        .build());

        // Thực hiện request PUT /users/{id}
        mockMvc.perform(put("/users/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userId").value("user1"))
                .andExpect(jsonPath("$.result.fullname").value("Nguyen Quang Huy"))
                .andExpect(jsonPath("$.result.email").value("huynew@sis.hust.edu.vn"));
    }

    @Test
    @WithMockUser(username = "use", roles = {"USER"})
    void updateUser_emailExisted() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("other@sis.hust.edu.vn") // giả lập email đã tồn tại
                .build();

        // Mock service ném AppException khi email đã tồn tại
        when(userService.updateUser(Mockito.eq("user1"), any()))
                .thenThrow(new AppException(ErrorCode.EMAIL_EXISTED));

        mockMvc.perform(put("/users/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // tương ứng với 400
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_EXISTED.getMessage()));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void updateUser_selfForbidden() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("Huy New")
                .build();

        // Nếu userId trong URL khác "user1", service sẽ ném UNAUTHORIZED
        when(userService.updateUser(Mockito.eq("otherUserId"), any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(put("/users/otherUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }



    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void updateUser_updateRoles_asNonAdmin() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .roles(List.of("ADMIN")) // user cố gắng update role
                .build();

        // Mock service ném AppException khi user không phải admin
        when(userService.updateUser(Mockito.eq("user1"), any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(put("/users/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()) // tương ứng với 403
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }



    // ================= DELETE USER =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_success() throws Exception {
        Mockito.doNothing().when(userService).deleteUser("1");

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("User deleted"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void deleteUser_unAuthorized() throws Exception {
        // Không mock doNothing(), để service ném AppException khi user không phải admin
        Mockito.doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(userService).deleteUser("1");

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden()) // tương ứng với 403
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }

    // ================= MY INFO =================
    @Test
    @WithMockUser(username = "me", roles = {"USER"})
    void getMyInfo_success() throws Exception {
        when(userService.getMyInfor())
                .thenReturn(UserResponse.builder()
                        .username("me")
                        .email("me@sis.hust.edu.vn")
                        .build());

        mockMvc.perform(get("/users/myInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("me"));
    }

    @Test
    @WithMockUser(username = "unknown", roles = {"USER"})
    void getMyInfo_userNotExist() throws Exception {
        // Mock service ném AppException khi user không tồn tại
        when(userService.getMyInfor())
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(get("/users/myInfo"))
                .andExpect(status().isNotFound()) // tương ứng với 404
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_EXIST.getMessage()));
    }

}
