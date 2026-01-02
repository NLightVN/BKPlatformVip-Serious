package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid Key", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(1002, "User already existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1005, "User not exist", HttpStatus.NOT_FOUND),
    SHOP_NOT_EXIST(1005, "Shop not exist", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_EMAIL(1008, "Your email must be HUST email", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1009, "Email already existed", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1010, "Invalid Token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1011, "Token has expired", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1012, "Email send failed", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1013, "Category existed", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXIST(1014, "Category not existed", HttpStatus.BAD_REQUEST),
    CATEGORY_USED_BY_PRODUCT(1015, "Category used by product", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXIST(1016, "Product does not exist", HttpStatus.BAD_REQUEST),
    CART_EMPTY(1017, "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_EXIST(1018, "Cart item not exist", HttpStatus.BAD_REQUEST),
    ORDER_NOT_EXIST(1019, "Order not exist", HttpStatus.BAD_REQUEST),
    WARD_NOT_FOUND(1020, "Ward not found", HttpStatus.BAD_REQUEST),
    INVALID_VALUE(1021, "Invalid value", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1022, "Address does not exist", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = httpStatusCode;
    }
}
