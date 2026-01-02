import React, { createContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);


    // Load user khi F5 trang
    useEffect(() => {
        const fetchInfo = async () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const res = await authService.getMyInfo();
                    if (res.code === 1000) setUser(res.result);
                } catch (error) {
                    console.error("Token expired");
                    localStorage.removeItem('token');
                }
            }
            setLoading(false);
        };
        fetchInfo();
    }, []);

    const login = async (username, password) => {
        const res = await authService.login(username, password);

        if (res.code === 1000) {
            localStorage.setItem('token', res.result.token);
            const infoRes = await authService.getMyInfo();
            if (infoRes.code === 1000) {
                const userInfo = infoRes.result;
                // Kiểm tra trạng thái BANNED
                if (userInfo.status === 'BANNED') {
                    localStorage.removeItem('token');
                    return { success: false, message: 'Tài khoản của bạn đã bị khóa (BANNED).' };
                }
                setUser(userInfo);
            }
            return { success: true };
        }

        // ✅ CỰC KỲ QUAN TRỌNG
        localStorage.removeItem('token');

        return {
            success: false,
            message: res.message || 'Sai mật khẩu',
        };
    };


    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
        window.location.href = '/';
    };

    const isAdmin = () => {
        if (!user || !user.roleNames) return false;
        return user.roleNames.includes('ADMIN');
    };

    return (
        <AuthContext.Provider value={{ user, setUser, login, logout, loading, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );

};