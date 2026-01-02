import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import axiosClient from '../api/axiosClient';
import { toast } from 'react-toastify';

const ResetPassword = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    const [loading, setLoading] = useState(false);
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    // Redirect if no token
    useEffect(() => {
        if (!token) {
            toast.error('Link không hợp lệ. Vui lòng yêu cầu link mới.');
            navigate('/forgot-password');
        }
    }, [token, navigate]);

    const handleResetPassword = async (e) => {
        e.preventDefault();

        if (!newPassword || !confirmPassword) {
            toast.warning('Vui lòng nhập đầy đủ thông tin');
            return;
        }

        if (newPassword !== confirmPassword) {
            toast.error('Mật khẩu xác nhận không khớp!');
            return;
        }

        if (newPassword.length < 6) {
            toast.warning('Mật khẩu phải có ít nhất 6 ký tự');
            return;
        }

        setLoading(true);
        try {
            await axiosClient.post('/auth/reset-password', null, {
                params: {
                    token: token,
                    newPassword: newPassword
                }
            });
            toast.success('Đặt lại mật khẩu thành công!');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            console.error(err);
            toast.error('Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn');
        } finally {
            setLoading(false);
        }
    };

    if (!token) return null;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '70vh', padding: '40px' }}>
                <div style={{
                    background: 'white',
                    padding: '50px',
                    borderRadius: '20px',
                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                    width: '100%',
                    maxWidth: '480px'
                }}>
                    <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                        <div style={{ fontSize: '48px', marginBottom: '15px' }}>🔐</div>
                        <h2 style={{ marginBottom: '10px', fontSize: '28px', color: '#333' }}>Đặt lại mật khẩu</h2>
                        <p style={{ color: '#666', fontSize: '14px' }}>
                            Nhập mật khẩu mới của bạn
                        </p>
                    </div>

                    <form onSubmit={handleResetPassword}>
                        <div className="form-group" style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500', color: '#333' }}>
                                Mật khẩu mới
                            </label>
                            <input
                                className="login-input"
                                type="password"
                                placeholder="Tối thiểu 6 ký tự"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                disabled={loading}
                                style={{
                                    width: '100%',
                                    padding: '12px 16px',
                                    fontSize: '15px',
                                    border: '1px solid #ddd',
                                    borderRadius: '8px'
                                }}
                            />
                        </div>

                        <div className="form-group" style={{ marginBottom: '25px' }}>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500', color: '#333' }}>
                                Xác nhận mật khẩu
                            </label>
                            <input
                                className="login-input"
                                type="password"
                                placeholder="Nhập lại mật khẩu mới"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                disabled={loading}
                                style={{
                                    width: '100%',
                                    padding: '12px 16px',
                                    fontSize: '15px',
                                    border: '1px solid #ddd',
                                    borderRadius: '8px'
                                }}
                            />
                        </div>

                        <button
                            className="login-btn"
                            type="submit"
                            disabled={loading}
                            style={{
                                width: '100%',
                                padding: '14px',
                                fontSize: '16px',
                                fontWeight: '600',
                                borderRadius: '8px',
                                border: 'none',
                                background: loading ? '#ccc' : '#c4161c',
                                color: 'white',
                                cursor: loading ? 'not-allowed' : 'pointer'
                            }}
                        >
                            {loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
                        </button>

                        <div style={{ textAlign: 'center', marginTop: '25px' }}>
                            <Link to="/login" style={{ color: '#666', textDecoration: 'none', fontSize: '14px' }}>
                                ← Quay lại đăng nhập
                            </Link>
                        </div>
                    </form>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default ResetPassword;
