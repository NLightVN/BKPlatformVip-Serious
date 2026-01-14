import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { shopService } from '../services/shopService';
import { AuthContext } from '../context/AuthContext';

const SellerChannel = () => {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();
    const [shops, setShops] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }

        // Lấy danh sách shop của user
        shopService.getShopByOwner(user.username)
            .then(res => {
                if (res.code === 1000) {
                    setShops(res.result || []);
                }
            })
            .catch(err => {
                console.error("Error fetching shops:", err);
                setShops([]);
            })
            .finally(() => setLoading(false));
    }, [user, navigate]);

    if (!user) {
        return (
            <div className="app-container">
                <Header />
                <main className="main-content">Vui lòng đăng nhập...</main>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="app-container">
                <Header />
                <main className="main-content">Đang tải...</main>
            </div>
        );
    }

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                <h2 style={{ marginBottom: '30px', color: '#c4161c' }}>Kênh Người Bán</h2>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                    gap: '20px',
                    marginTop: '20px'
                }}>
                    {/* Hiển thị các shop hiện có */}
                    {shops.map(shop => (
                        <div
                            key={shop.shopId}
                            onClick={() => navigate(`/shop/${shop.shopId}`)}
                            style={{
                                border: '2px solid #e0e0e0',
                                borderRadius: '10px',
                                padding: '20px',
                                cursor: 'pointer',
                                transition: 'all 0.3s ease',
                                background: 'white',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                gap: '15px'
                            }}
                            onMouseEnter={e => {
                                e.currentTarget.style.borderColor = '#c4161c';
                                e.currentTarget.style.transform = 'translateY(-5px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(196,22,28,0.2)';
                            }}
                            onMouseLeave={e => {
                                e.currentTarget.style.borderColor = '#e0e0e0';
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                            }}
                        >
                            {/* Logo/Avatar */}
                            <div style={{
                                width: '80px',
                                height: '80px',
                                background: '#c4161c',
                                borderRadius: '50%',
                                color: 'white',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '32px',
                                fontWeight: 'bold'
                            }}>
                                {shop.name.charAt(0).toUpperCase()}
                            </div>

                            {/* Tên Shop */}
                            <h3 style={{
                                margin: 0,
                                fontSize: '18px',
                                textAlign: 'center',
                                color: '#333'
                            }}>
                                {shop.name}
                            </h3>

                            {/* Địa chỉ */}
                            {shop.address && (
                                <p style={{
                                    margin: 0,
                                    fontSize: '13px',
                                    color: '#666',
                                    textAlign: 'center'
                                }}>
                                    📍 {shop.address.district?.fullName}
                                </p>
                            )}
                        </div>
                    ))}

                    {/* Card Tạo Shop Mới */}
                    <div
                        onClick={() => navigate('/create-shop')}
                        style={{
                            border: '2px dashed #c4161c',
                            borderRadius: '10px',
                            padding: '20px',
                            cursor: 'pointer',
                            transition: 'all 0.3s ease',
                            background: '#fff5f5',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '15px',
                            minHeight: '200px'
                        }}
                        onMouseEnter={e => {
                            e.currentTarget.style.background = '#ffe5e5';
                            e.currentTarget.style.transform = 'translateY(-5px)';
                        }}
                        onMouseLeave={e => {
                            e.currentTarget.style.background = '#fff5f5';
                            e.currentTarget.style.transform = 'translateY(0)';
                        }}
                    >
                        {/* Icon dấu + */}
                        <div style={{
                            width: '80px',
                            height: '80px',
                            border: '3px dashed #c4161c',
                            borderRadius: '50%',
                            color: '#c4161c',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '48px',
                            fontWeight: 'bold'
                        }}>
                            +
                        </div>

                        <h3 style={{
                            margin: 0,
                            fontSize: '18px',
                            color: '#c4161c',
                            fontWeight: 'bold'
                        }}>
                            Tạo Shop Mới
                        </h3>
                    </div>
                </div>

                {shops.length === 0 && (
                    <div style={{
                        textAlign: 'center',
                        marginTop: '40px',
                        color: '#666',
                        fontSize: '16px'
                    }}>
                        <p>Bạn chưa có shop nào. Hãy tạo shop đầu tiên của bạn!</p>
                    </div>
                )}
            </main>
            <Footer />
        </div>
    );
};

export default SellerChannel;
