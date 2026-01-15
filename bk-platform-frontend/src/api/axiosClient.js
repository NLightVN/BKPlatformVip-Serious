import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080', // URL Backend Spring Boot của bạn
});

// Interceptor: Tự động gắn Token vào mỗi request nếu đã đăng nhập
axiosClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        // SỬA LẠI DÒNG NÀY: Thêm dấu backtick `
        config.headers.Authorization = `Bearer ${token}`;
    }

    // If sending FormData, remove Content-Type to let browser set it with boundary
    if (config.data instanceof FormData) {
        delete config.headers['Content-Type'];
    } else if (config.data) {
        config.headers['Content-Type'] = 'application/json';
    }

    return config;
});

// Interceptor: Xử lý lỗi chung
axiosClient.interceptors.response.use(
    (response) => response.data ? response.data : response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        throw error;
    }
);

export default axiosClient;