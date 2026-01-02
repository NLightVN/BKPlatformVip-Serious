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
                if(res.code === 1000) setUser(res.result);
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
        if(infoRes.code === 1000) setUser(infoRes.result);
        return true;
    }
    return false;
};

const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    window.location.href = '/';
};

return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
        {children}
    </AuthContext.Provider>
);

};