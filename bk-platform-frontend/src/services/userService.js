import axiosClient from "../api/axiosClient";

export const userService = {
    updateUser: (userId, userData) => {
        // userData format: { fullname, email, address: { name, phone, addressDetail, wardCode } }
        return axiosClient.put(`/users/${userId}`, userData);
    },

    getUserById: (userId) => {
        return axiosClient.get(`/users/${userId}`);
    },

    getMyInfo: () => {
        return axiosClient.get('/users/myInfo');
    }
};
