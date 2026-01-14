package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // System / uncategorized
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1000, "Validation failed", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(1001, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    JSON_PARSE_ERROR(1002, "Invalid JSON format", HttpStatus.BAD_REQUEST),

    // Authentication & Authorization
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    USER_BANNED(1009, "Tài khoản của bạn đã bị cấm. Bạn không thể đặt hàng.", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1010, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1011, "Token has expired", HttpStatus.BAD_REQUEST),

    // User
    USERNAME_EXISTED(1200, "User already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1201, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1202, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1203, "User does not exist", HttpStatus.NOT_FOUND),
    INVALID_EMAIL(1204, "Your email must be HUST email", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1205, "Email already existed", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1206, "Email send failed", HttpStatus.BAD_REQUEST),
    ROLE_EXISTED(1207, "Role already exists", HttpStatus.BAD_REQUEST),

    // Shop
    SHOP_NOT_EXIST(1300, "Shop does not exist", HttpStatus.NOT_FOUND),

    // Category
    CATEGORY_EXISTED(1400, "Category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXIST(1401, "Category does not exist", HttpStatus.BAD_REQUEST),
    CATEGORY_USED_BY_PRODUCT(1402, "Category is used by product", HttpStatus.BAD_REQUEST),

    // Product
    PRODUCT_NOT_EXIST(1500, "Product does not exist", HttpStatus.NOT_FOUND),

    // Cart
    CART_EMPTY(1600, "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_EXIST(1601, "Cart item does not exist", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_EXIST(1700, "Order does not exist", HttpStatus.BAD_REQUEST),

    // Address/Ward
    WARD_NOT_FOUND(1800, "Ward not found", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1801, "Address does not exist", HttpStatus.BAD_REQUEST),

    // Generic invalid value
    INVALID_VALUE(1900, "Invalid value", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}

