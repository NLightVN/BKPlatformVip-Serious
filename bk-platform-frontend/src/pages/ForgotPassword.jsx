import React, { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import axiosClient from '../api/axiosClient';
import { toast } from 'react-toastify';

const ForgotPassword = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    // Two modes: Request Reset vs Reset Password
    const [step, setStep] = useState(token ? 'reset' : 'request');
    const [loading, setLoading] = useState(false);

    // Form states
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const handleRequestReset = async (e) => {
        e.preventDefault();
        if (!email) {
            toast.warning('Vui lòng nhập email');
            return;
        }

        setLoading(true);
        try {
            await axiosClient.post('/auth/forgot-password', null, {
                params: { email }
            });
            toast.success('Đã gửi link đặt lại mật khẩu vào email của bạn!');
            setStep('sent');
        } catch (err) {
            toast.error('Email không tồn tại trong hệ thống');
        } finally {
            setLoading(false);
        }
    };

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
            setTimeout(() => navigate('/login'), 1500);
        } catch (err) {
            toast.error('Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', padding: '40px' }}>
                <div style={{
                    background: 'white',
                    padding: '40px',
                    borderRadius: '16px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
                    width: '100%',
                    maxWidth: '450px'
                }}>
                    {step === 'request' && (
                        <>
                            <h2 style={{ marginBottom: '10px', fontSize: '28px' }}>Quên mật khẩu?</h2>
                            <p style={{ color: '#666', marginBottom: '30px', fontSize: '14px' }}>
                                Nhập email của bạn và chúng tôi sẽ gửi link đặt lại mật khẩu
                            </p>

                            <form onSubmit={handleRequestReset}>
                                <div className="form-group">
                                    <label>Email</label>
                                    <input
                                        className="login-input"
                                        type="email"
                                        placeholder="email@example.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        disabled={loading}
                                    />
                                </div>

                                <button
                                    className="login-btn"
                                    type="submit"
                                    disabled={loading}
                                    style={{ width: '100%', marginTop: '20px' }}
                                >
                                    {loading ? 'Đang gửi...' : 'Gửi link đặt lại'}
                                </button>

                                <div style={{ textAlign: 'center', marginTop: '20px' }}>
                                    <Link to="/login" style={{ color: '#c4161c', textDecoration: 'none', fontSize: '14px' }}>
                                        ← Quay lại đăng nhập
                                    </Link>
                                </div>
                            </form>
                        </>
                    )}

                    {step === 'sent' && (
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: '60px', marginBottom: '20px' }}>📧</div>
                            <h2 style={{ marginBottom: '15px', color: '#4CAF50' }}>Email đã được gửi!</h2>
                            <p style={{ color: '#666', marginBottom: '30px', lineHeight: '1.6' }}>
                                Chúng tôi đã gửi link đặt lại mật khẩu đến<br />
                                <strong>{email}</strong><br /><br />
                                Vui lòng kiểm tra hộp thư và làm theo hướng dẫn.
                            </p>
                            <Link to="/login">
                                <button className="btn" style={{ width: '100%' }}>
                                    Quay lại đăng nhập
                                </button>
                            </Link>
                        </div>
                    )}

                    {step === 'reset' && (
                        <>
                            <h2 style={{ marginBottom: '10px', fontSize: '28px' }}>Đặt lại mật khẩu</h2>
                            <p style={{ color: '#666', marginBottom: '30px', fontSize: '14px' }}>
                                Nhập mật khẩu mới của bạn
                            </p>

                            <form onSubmit={handleResetPassword}>
                                <div className="form-group">
                                    <label>Mật khẩu mới</label>
                                    <input
                                        className="login-input"
                                        type="password"
                                        placeholder="Tối thiểu 6 ký tự"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        disabled={loading}
                                    />
                                </div>

                                <div className="form-group">
                                    <label>Xác nhận mật khẩu</label>
                                    <input
                                        className="login-input"
                                        type="password"
                                        placeholder="Nhập lại mật khẩu mới"
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        disabled={loading}
                                    />
                                </div>

                                <button
                                    className="login-btn"
                                    type="submit"
                                    disabled={loading}
                                    style={{ width: '100%', marginTop: '20px' }}
                                >
                                    {loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
                                </button>
                            </form>
                        </>
                    )}
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default ForgotPassword;
