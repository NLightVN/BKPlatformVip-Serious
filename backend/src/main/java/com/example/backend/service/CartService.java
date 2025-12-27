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
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
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
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;

    CartMapper cartMapper;
    CartItemMapper cartItemMapper;

    @Transactional
    public CartResponse addToCart(CartRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXIST));
        var product = productRepository.findById(request.getProductId())
                .orElseThrow(()-> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

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
            cartItemRepository.save(existingItem); // Lưu item cũ
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart) // Quan trọng: set cha cho con
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            cart.getItems().add(newItem); // Add vào list cha
            cartItemRepository.save(newItem); // <--- QUAN TRỌNG: Lưu item mới trực tiếp
        }

        cart.setTotalAmount(calculateTotalAmount(cart));

        // Dùng saveAndFlush để đảm bảo dữ liệu ghi xuống DB ngay lập tức,
        // giúp TestScenarioRunner ở Thread khác có thể đọc được ngay.
        cartRepository.saveAndFlush(cart);

        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(String userId, String cartItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXIST));
        Cart cart = user.getCart();
        if (cart == null) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }else{
            CartItem removeItem = cart.getItems()
                    .stream().filter(item->item.getId().equals(cartItemId))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXIST));
            cart.getItems().remove(removeItem);
        }
        cart.setTotalAmount(calculateTotalAmount(cart));
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(String userId) {
        User user =  userRepository.findByUserId(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXIST));
        Cart cart = user.getCart();
        if (cart == null) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }
        cart.getItems().clear();
        cart.setTotalAmount(0);
        return cartMapper.toCartResponse(cart);
    }
    @Transactional
    public CartResponse getCartByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder()
                    .user(user)
                    .totalAmount(0)
                    .build();
            cartRepository.save(cart);
        }

        return cartMapper.toCartResponse(cart);
    }


    private double calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

}
