import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const result = await login(username, password);
            if (result.success) {
                toast.success("Đăng nhập thành công!");
                navigate('/shopping');
            } else {
                const msg = result.message || "Sai tài khoản hoặc mật khẩu";
                toast.error(msg);
                setError(msg);
                setLoading(false);
            }
        } catch (err) {
            let msg = err.message || "Lỗi kết nối Server";
            if (err.response) {
                msg = err.response.data?.message || (typeof err.response.data === 'string' ? err.response.data : "Sai tài khoản hoặc mật khẩu");
            }
            toast.error(msg);
            setError(msg);
            setLoading(false);
        }
    };

    return (
        <div className="app-container">
            <Header />

            <main className="main-content" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <div className="login-card">
                    <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Đăng nhập</h2>
                    {error && (
                        <div style={{
                            backgroundColor: '#f8d7da',
                            color: '#721c24',
                            padding: '10px',
                            borderRadius: '5px',
                            marginBottom: '15px',
                            textAlign: 'center',
                            fontSize: '14px',
                            border: '1px solid #f5c6cb'
                        }}>
                            {error}
                        </div>
                    )}
                    <form onSubmit={handleLogin}>
                        <div className="form-group">
                            <label>Tên đăng nhập:</label>
                            <input className="login-input" value={username} onChange={e => setUsername(e.target.value)} required />
                        </div>
                        <div className="form-group">
                            <label>Mật khẩu:</label>
                            <input type="password" className="login-input" value={password} onChange={e => setPassword(e.target.value)} required />
                        </div>
                        <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }} disabled={loading}>
                            {loading ? 'Đang xử lý...' : 'Đăng nhập'}
                        </button>
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