import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';
import { cartService } from '../services/cartService';
import { toast } from 'react-toastify';

export const ShoppingCartContext = createContext();

export const CartProvider = ({ children }) => {
    const { user } = useContext(AuthContext);
    const [cart, setCart] = useState(null);

    const fetchCart = async () => {
        if (user?.userId) {
            try {
                const res = await cartService.getCart(user.userId);
                if (res.code === 1000) setCart(res.result);
            } catch (err) { console.error(err); }
        } else {
            setCart(null);
        }
    };

    useEffect(() => { fetchCart(); }, [user]);

    const addToCart = async (productId, quantity = 1) => {
        if (!user) { toast.warning("Vui lòng đăng nhập!"); return; }
        try {
            await cartService.addToCart(user.userId, productId, quantity);
            await fetchCart();
        } catch (e) { toast.error("Lỗi thêm giỏ hàng"); }
    };

    // Hàm mới: Cập nhật số lượng (Hiện tại Backend chưa có API update direct,
    // nên ta dùng trick: Xóa rồi thêm lại hoặc gọi add với số lượng bù trừ. 
    // Tốt nhất Backend nên có API PUT /cart/update. 
    // Ở đây mình giả lập update bằng cách xóa đi thêm lại số lượng mới để đảm bảo đúng logic hiển thị)
    const updateQuantity = async (productId, newQuantity) => {
        if (!user || newQuantity < 1) return;
        try {
            await cartService.updateCartItem(user.userId, productId, newQuantity);
            await fetchCart();
        } catch (e) {
            console.error('Update quantity error:', e);
            toast.error("Lỗi cập nhật số lượng");
        }
    };

    const removeFromCart = async (productId) => {
        if (!user) return;
        await cartService.removeFromCart(user.userId, productId);
        await fetchCart();
    };

    return (
        <ShoppingCartContext.Provider value={{ cart, addToCart, removeFromCart, updateQuantity, fetchCart }}>
            {children}
        </ShoppingCartContext.Provider>
    );
};