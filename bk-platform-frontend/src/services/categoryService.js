import axiosClient from "../api/axiosClient";

export const categoryService = {
    getAllCategories: () => {
        return axiosClient.get('/categories');
    },
    getCategoryById: (id) => {
        return axiosClient.get(`/categories/id/${id}`);
    },
    searchCategories: (keyword) => {
        return axiosClient.get(`/categories/search/${keyword}`);
    },
    createCategory: (name) => {
        return axiosClient.post('/categories', { name });
    },
    deleteCategory: (categoryName) => {
        return axiosClient.delete(`/categories/${categoryName}`);
    }
};
