package com.example.backend.service;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.CartItemMapper;
import com.example.backend.mapper.CartMapper;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    UserRepository userRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;

    CartMapper cartMapper;

    @Transactional
    public CartResponse addToCart(CartRequest request, String userId) {
        User user = authorizeCartAccess(userId);

        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_VALUE);
        }

        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder()
                    .user(user)
                    .totalAmount(0)
                    .items(new ArrayList<>())
                    .build();
            user.setCart(cart);
            cart = cartRepository.save(cart); // Lưu Cart cha trước để có ID
        }

        CartItem existingItem = cart.getItems()
                .stream().filter(item-> item.getProduct().getProductId()
                        .equals(request.getProductId())).findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(request.getQuantity() + existingItem.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart) // Quan trọng: set cha cho con
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            cart.getItems().add(newItem); // Add vào list cha
        }

        cart.setTotalAmount(calculateTotalAmount(cart));

        // Dùng saveAndFlush để đảm bảo dữ liệu ghi xuống DB ngay lập tức,
        // giúp TestScenarioRunner ở Thread khác có thể đọc được ngay.
        cartRepository.saveAndFlush(cart);
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(String userId, String productId) {
        User user = authorizeCartAccess(userId);

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        CartItem removeItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct().getProductId().equals(productId)
                )
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXIST));

        cart.getItems().remove(removeItem);

        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);

        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(String userId) {
        User user = authorizeCartAccess(userId);

        Cart cart = user.getCart();
        if (cart == null) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }
        cart.getItems().clear();
        cart.setTotalAmount(0);
        cartRepository.save(cart);
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse getCartByUser(String userId) {
        User user = authorizeCartAccess(userId);;

        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder()
                    .user(user)
                    .totalAmount(0)
                    .build();
            cartRepository.save(cart);
            user.setCart(cart);
        }

        return cartMapper.toCartResponse(cart);
    }


    private double calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    private User authorizeCartAccess(String userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        String currentUsername = SecurityUtil.getCurrentUsername();

        if (!SecurityUtil.hasRole("ADMIN")
                && !targetUser.getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return targetUser;
    }


}
