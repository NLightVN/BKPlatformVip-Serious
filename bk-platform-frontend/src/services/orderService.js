import axiosClient from "../api/axiosClient";

export const orderService = {

    calculateShippingFee: (data) => {
        return axiosClient.post('/shipping/calculate', data);
    },
    checkout: (productIds) => {
        return axiosClient.post('/orders/checkout/selected', { productIds });
    },
    buyNow: (productId, quantity) => {
        return axiosClient.post('/orders/buy-now', { productId, quantity });
    },
    getHistory: (userId) => axiosClient.get(`/orders/user/${userId}`),

    // API cho Seller xem đơn hàng của shop
    getOrdersByShop: (shopId) => axiosClient.get(`/orders/shop/${shopId}`),

    // Yêu cầu hủy đơn (User)
    requestCancel: (orderId) => axiosClient.post(`/orders/${orderId}/cancel-request`),

    // Phản hồi yêu cầu hủy (Shop)
    replyCancel: (orderId, accept) => axiosClient.post(`/orders/${orderId}/cancel-response`, accept)
};