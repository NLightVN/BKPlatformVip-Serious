package com.example.backend.constant;


import lombok.Getter;

@Getter
public enum PredefinedPermission {
    // USER management
    USER_READ("USER_READ", "Read user information"),
    USER_CREATE("USER_CREATE", "Create new user"),
    USER_UPDATE("USER_UPDATE", "Update existing user"),
    USER_DELETE("USER_DELETE", "Delete user"),

    // ROLE management
    ROLE_READ("ROLE_READ", "Read role information"),
    ROLE_CREATE("ROLE_CREATE", "Create new role"),
    ROLE_UPDATE("ROLE_UPDATE", "Update role"),
    ROLE_DELETE("ROLE_DELETE", "Delete role"),

    // PERMISSION management
    PERMISSION_READ("PERMISSION_READ", "Read permissions"),
    PERMISSION_CREATE("PERMISSION_CREATE", "Create permission"),
    PERMISSION_DELETE("PERMISSION_DELETE", "Delete permission"),

    // PRODUCT or resource actions (ví dụ nếu hệ thống có)
    PRODUCT_READ("PRODUCT_READ", "Read product info"),
    PRODUCT_CREATE("PRODUCT_CREATE", "Add new product"),
    PRODUCT_UPDATE("PRODUCT_UPDATE", "Edit product"),
    PRODUCT_DELETE("PRODUCT_DELETE", "Delete product");

    private final String name;
    private final String description;

    PredefinedPermission(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

