import axiosClient from "../api/axiosClient";

export const productService = {
    getAllProducts: () => {
        return axiosClient.get('/products');
    },
    getProductById: (id) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/products/${id}`);
    },
    searchProducts: (keyword) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/products/search/${keyword}`);
    },
    getProductsByShop: (shopId) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/products/shop/${shopId}`);
    },
    createProduct: (productData) => {
        // productData format: { shopId, name, price, weight, brand, description, categoryNames }
        return axiosClient.post('/products', productData);
    },
    updateProduct: (productId, productData) => {
        // productData format: { shopId, name, price, weight, brand, description, categoryNames }
        return axiosClient.put(`/products/${productId}`, productData);
    },
    deleteProduct: (productId) => {
        return axiosClient.delete(`/products/${productId}`);
    },
    uploadProductImages: (productId, files) => {
        const formData = new FormData();

        // Append all data together in one loop
        files.forEach((file) => {
            formData.append('files', file);
        });

        return axiosClient.post(`/products/${productId}/images/upload`, formData);
    }
};