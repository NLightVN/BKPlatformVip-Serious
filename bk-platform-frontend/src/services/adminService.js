import axios from 'axios';

const API_URL = 'http://localhost:8080';

const getAuthHeaders = () => ({
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
});

export const adminService = {
    // User Management
    getAllUsers: async () => {
        const response = await axios.get(`${API_URL}/admin/users`, getAuthHeaders());
        return response.data;
    },

    getUserDetail: async (userId) => {
        const response = await axios.get(`${API_URL}/admin/users/${userId}`, getAuthHeaders());
        return response.data;
    },

    banUser: async (userId, banned) => {
        const response = await axios.put(
            `${API_URL}/admin/users/${userId}/ban`,
            { banned },
            getAuthHeaders()
        );
        return response.data;
    },

    // Shop Management
    getShopById: async (shopId) => {
        const response = await axios.get(`${API_URL}/admin/shops/${shopId}`, getAuthHeaders());
        return response.data;
    },

    banShop: async (shopId, banned) => {
        const response = await axios.put(
            `${API_URL}/admin/shops/${shopId}/ban`,
            { banned },
            getAuthHeaders()
        );
        return response.data;
    },

    // Product Management
    getAllProducts: async () => {
        const response = await axios.get(`${API_URL}/admin/products`, getAuthHeaders());
        return response.data;
    },

    banProduct: async (productId, banned) => {
        const response = await axios.put(
            `${API_URL}/admin/products/${productId}/ban`,
            { banned },
            getAuthHeaders()
        );
        return response.data;
    },

    // Order Management
    getOrderById: async (orderId) => {
        const response = await axios.get(`${API_URL}/admin/orders/${orderId}`, getAuthHeaders());
        return response.data;
    },
};
