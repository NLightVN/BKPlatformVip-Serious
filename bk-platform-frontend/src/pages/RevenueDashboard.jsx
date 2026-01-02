import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { shopService } from '../services/shopService';
import { AuthContext } from '../context/AuthContext';

const RevenueDashboard = () => {
    const { shopId } = useParams();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const [revenue, setRevenue] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }

        shopService.getShopRevenue(shopId)
            .then(res => {
                if (res.code === 1000) {
                    setRevenue(res.result);
                }
            })
            .catch(err => {
                console.error('Error fetching revenue:', err);
            })
            .finally(() => setLoading(false));
    }, [shopId, user, navigate]);

    if (loading) {
        return (
            <div className="app-container">
                <Header />
                <main className="main-content">Đang tải...</main>
            </div>
        );
    }

    if (!revenue) {
        return (
            <div className="app-container">
                <Header />
                <main className="main-content">Không thể tải dữ liệu doanh thu</main>
            </div>
        );
    }

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
                    <h2 style={{ color: '#c4161c' }}>📊 Báo cáo Doanh thu</h2>
                    <button
                        onClick={() => navigate(`/shop/${shopId}`)}
                        style={{
                            background: '#6c757d',
                            color: 'white',
                            border: 'none',
                            padding: '10px 20px',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontWeight: '600'
                        }}
                    >
                        ← Quay lại Shop
                    </button>
                </div>

                {/* Summary Cards */}
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '20px',
                    marginBottom: '40px'
                }}>
                    {/* Total Revenue */}
                    <div style={{
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        color: 'white',
                        padding: '25px',
                        borderRadius: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}>
                        <div style={{ fontSize: '14px', opacity: 0.9, marginBottom: '8px' }}>Tổng Doanh Thu</div>
                        <div style={{ fontSize: '32px', fontWeight: 'bold' }}>
                            {revenue.totalRevenue.toLocaleString()} đ
                        </div>
                    </div>

                    {/* Total Orders */}
                    <div style={{
                        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                        color: 'white',
                        padding: '25px',
                        borderRadius: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}>
                        <div style={{ fontSize: '14px', opacity: 0.9, marginBottom: '8px' }}>Tổng Đơn Hàng</div>
                        <div style={{ fontSize: '32px', fontWeight: 'bold' }}>
                            {revenue.totalOrders}
                        </div>
                    </div>

                    {/* Average Order Value */}
                    <div style={{
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        color: 'white',
                        padding: '25px',
                        borderRadius: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}>
                        <div style={{ fontSize: '14px', opacity: 0.9, marginBottom: '8px' }}>Giá Trị TB/Đơn</div>
                        <div style={{ fontSize: '32px', fontWeight: 'bold' }}>
                            {revenue.averageOrderValue.toLocaleString()} đ
                        </div>
                    </div>
                </div>

                {/* Order Status Breakdown */}
                <div style={{
                    background: 'white',
                    padding: '30px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
                }}>
                    <h3 style={{ marginBottom: '25px' }}>Phân tích theo Trạng thái Đơn hàng</h3>

                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                        gap: '15px'
                    }}>
                        {/* Pending */}
                        <div style={{
                            padding: '20px',
                            border: '2px solid #ffc107',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Đang xử lý</div>
                            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#ffc107' }}>
                                {revenue.pendingOrders}
                            </div>
                        </div>

                        {/* Awaiting Pickup */}
                        <div style={{
                            padding: '20px',
                            border: '2px solid #17a2b8',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Chờ lấy hàng</div>
                            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#17a2b8' }}>
                                {revenue.awaitingPickupOrders}
                            </div>
                        </div>

                        {/* Shipped */}
                        <div style={{
                            padding: '20px',
                            border: '2px solid #007bff',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Đang giao</div>
                            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#007bff' }}>
                                {revenue.shippedOrders}
                            </div>
                        </div>

                        {/* Delivered */}
                        <div style={{
                            padding: '20px',
                            border: '2px solid #28a745',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Hoàn thành</div>
                            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#28a745' }}>
                                {revenue.deliveredOrders}
                            </div>
                        </div>

                        {/* Cancelled */}
                        <div style={{
                            padding: '20px',
                            border: '2px solid #dc3545',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Đã hủy</div>
                            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#dc3545' }}>
                                {revenue.cancelledOrders}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Simple Visual Bar Chart */}
                <div style={{
                    background: 'white',
                    padding: '30px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                    marginTop: '20px'
                }}>
                    <h3 style={{ marginBottom: '25px' }}>Biểu đồ Đơn hàng</h3>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {[
                            { label: 'Đang xử lý', count: revenue.pendingOrders, color: '#ffc107' },
                            { label: 'Chờ lấy hàng', count: revenue.awaitingPickupOrders, color: '#17a2b8' },
                            { label: 'Đang giao', count: revenue.shippedOrders, color: '#007bff' },
                            { label: 'Hoàn thành', count: revenue.deliveredOrders, color: '#28a745' },
                            { label: 'Đã hủy', count: revenue.cancelledOrders, color: '#dc3545' }
                        ].map((item, idx) => {
                            const percentage = revenue.totalOrders > 0
                                ? (item.count / revenue.totalOrders * 100)
                                : 0;

                            return (
                                <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                                    <div style={{ width: '120px', fontSize: '14px', color: '#666' }}>
                                        {item.label}
                                    </div>
                                    <div style={{ flex: 1, background: '#f0f0f0', borderRadius: '10px', height: '30px', position: 'relative', overflow: 'hidden' }}>
                                        <div style={{
                                            width: `${percentage}%`,
                                            height: '100%',
                                            background: item.color,
                                            borderRadius: '10px',
                                            transition: 'width 0.3s ease'
                                        }} />
                                    </div>
                                    <div style={{ width: '80px', textAlign: 'right', fontWeight: 'bold' }}>
                                        {item.count} ({percentage.toFixed(1)}%)
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default RevenueDashboard;
