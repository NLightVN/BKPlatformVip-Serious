import axiosClient from "../api/axiosClient";

export const cartService = {
    getCart: (userId) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/cart/${userId}`);
    },
    addToCart: (userId, productId, quantity) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.post(`/cart/add/${userId}`, { productId, quantity });
    },
    updateCartItem: (userId, productId, quantity) => {
        return axiosClient.put(`/cart/update/${userId}`, { productId, quantity });
    },
    removeFromCart: (userId, productId) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.delete(`/cart/remove/${userId}/${productId}`);
    },
    clearCart: (userId) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.delete(`/cart/clear/${userId}`);
    }
};