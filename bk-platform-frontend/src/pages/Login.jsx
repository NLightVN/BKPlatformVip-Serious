import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const result = await login(username, password);
            if (result.success) {
                toast.success("Đăng nhập thành công!");
                navigate('/shopping');
            } else {
                toast.error(result.message || "Sai tài khoản hoặc mật khẩu");
            }
        } catch (err) {
            toast.error(err.message || "Lỗi kết nối Server");
        }
    };

    return (
        <div className="app-container">
            <Header />

            <main className="main-content" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <div className="login-card">
                    <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Đăng nhập</h2>
                    <form onSubmit={handleLogin}>
                        <div className="form-group">
                            <label>Tên đăng nhập:</label>
                            <input className="login-input" value={username} onChange={e => setUsername(e.target.value)} required />
                        </div>
                        <div className="form-group">
                            <label>Mật khẩu:</label>
                            <input type="password" className="login-input" value={password} onChange={e => setPassword(e.target.value)} required />
                        </div>
                        <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }}>Đăng nhập</button>
                    </form>
                    <p style={{ textAlign: 'center', marginTop: '15px', fontSize: '14px' }}>
                        Chưa có tài khoản? <Link to="/signup" style={{ color: '#c4161c', fontWeight: 'bold' }}>Đăng ký ngay</Link>
                    </p>
                    <p style={{ textAlign: 'center', marginTop: '10px', fontSize: '14px' }}>
                        <Link to="/forgot-password" style={{ color: '#666', textDecoration: 'none' }}>Quên mật khẩu?</Link>
                    </p>
                </div>
            </main>

            <Footer />
        </div>
    );
};

export default Login;