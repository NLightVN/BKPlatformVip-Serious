import axiosClient from "../api/axiosClient";

export const shopService = {
    getShopById: (shopId) => {
        return axiosClient.get(`/shops/${shopId}`);
    },
    getAllShops: () => {
        return axiosClient.get('/shops');
    },
    getShopByOwner: (ownerUsername) => {
        return axiosClient.get(`/shops/owner/${ownerUsername}`);
    },
    // --- THÊM MỚI ---
    createShop: (data) => {
        // data format: { name, address: { ... } }
        return axiosClient.post('/shops', data);
    }
};