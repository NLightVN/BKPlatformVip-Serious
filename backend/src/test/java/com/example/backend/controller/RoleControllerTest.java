package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RoleService roleService;

    @MockBean
    CustomJwtDecoder customJwtDecoder;


    // ================= CREATE ROLE =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createRole_success() throws Exception {
        RoleRequest request = new RoleRequest("ROLE_USER", "Normal user");

        when(roleService.create(any(RoleRequest.class)))
                .thenReturn(new RoleResponse("ROLE_USER", "Normal user"));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("ROLE_USER"))
                .andExpect(jsonPath("$.result.description").value("Normal user"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createRole_nonAdmin_forbidden() throws Exception {
        RoleRequest request = new RoleRequest("ROLE_USER", "Normal user");

        Mockito.doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(roleService).create(any(RoleRequest.class));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_roleAlreadyExists() throws Exception {
        RoleRequest request = new RoleRequest("EXISTED ROLE", "Normal user");
        Mockito.doThrow(new AppException(ErrorCode.ROLE_EXISTED))
                .when(roleService).create(any());

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.ROLE_EXISTED.getMessage()));
    }


    // ================= GET ALL ROLES =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllRoles_success() throws Exception {
        when(roleService.getAll()).thenReturn(List.of(
                new RoleResponse("ROLE_USER", "Normal user"),
                new RoleResponse("ROLE_ADMIN", "Administrator")
        ));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].name").value("ROLE_USER"))
                .andExpect(jsonPath("$.result[1].name").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllRoles_nonAdmin_forbidden() throws Exception {
        Mockito.doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(roleService).getAll();

        mockMvc.perform(get("/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }


    // ================= DELETE ROLE =================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteRole_success() throws Exception {
        Mockito.doNothing().when(roleService).delete("ROLE_USER");

        mockMvc.perform(delete("/roles/ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteRole_nonAdmin_forbidden() throws Exception {
        Mockito.doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(roleService).delete("ROLE_USER");

        mockMvc.perform(delete("/roles/ROLE_USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteRole_notExist() throws Exception {
        Mockito.doThrow(new AppException(ErrorCode.USER_NOT_EXIST))
                .when(roleService).delete("ROLE_UNKNOWN");

        mockMvc.perform(delete("/roles/ROLE_UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_EXIST.getMessage()));
    }

}
