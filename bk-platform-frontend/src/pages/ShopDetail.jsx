import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { shopService } from '../services/shopService';
import { productService } from '../services/productService';
import { orderService } from '../services/orderService';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const ShopDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const [shop, setShop] = useState(null);
    const [products, setProducts] = useState([]); // Khôi phục products
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true); // Khôi phục loading
    const [activeTab, setActiveTab] = useState('products'); // products | orders

    const isOwner = user && shop && user.username === shop.ownerUsername;

    useEffect(() => {
        // 1. Lấy thông tin Shop
        shopService.getShopById(id).then(res => {
            const data = res.result || res;
            setShop(data);
        });

        // 2. Lấy sản phẩm
        productService.getProductsByShop(id).then(res => {
            if (res.code === 1000) setProducts(res.result);
        }).finally(() => setLoading(false));
    }, [id]);

    // Fetch orders khi chuyển sang tab Orders
    useEffect(() => {
        if (activeTab === 'orders' && isOwner) {
            orderService.getOrdersByShop(id).then(res => {
                if (res.code === 1000) {
                    console.log("Orders List:", res.result);
                    setOrders(res.result);
                }
            });
        }
    }, [activeTab, isOwner, id]);

    const handleReplyCancel = async (orderId, accept) => {
        if (!window.confirm(accept ? "Đồng ý hủy đơn hàng này?" : "Từ chối yêu cầu hủy?")) return;
        try {
            const res = await orderService.replyCancel(orderId, accept);
            if (res.code === 1000) {
                toast.success(accept ? "Đã hủy đơn hàng" : "Đã từ chối yêu cầu");
                // Refresh list
                const updatedOrders = orders.map(o => {
                    if (o.orderId === orderId) {
                        return { ...o, status: accept ? 'CANCELLED' : o.status, cancellationRequested: false };
                    }
                    return o;
                });
                setOrders(updatedOrders);
            } else {
                toast.error(res.message || "Lỗi xử lý");
            }
        } catch (err) {
            toast.error("Lỗi hệ thống");
        }
    };


    if (loading) return <div className="app-container"><Header /><main className="main-content">Đang tải...</main></div>;
    if (!shop) return <div className="app-container"><Header /><main className="main-content">Shop không tồn tại</main></div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                {/* SHOP HEADER */}
                <div className="shop-header">
                    <div style={{ width: '100px', height: '100px', background: '#c4161c', borderRadius: '50%', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '40px', fontWeight: 'bold' }}>
                        {shop.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <h1 style={{ marginBottom: '5px' }}>{shop.name}</h1>
                        <p style={{ color: '#666' }}>Chủ sở hữu: <strong>{shop.ownerUsername}</strong></p>
                        <p style={{ color: '#666' }}>
                            📍 {shop.address?.addressDetail}, {shop.address?.ward?.fullName}, {shop.address?.district?.fullName}
                        </p>
                    </div>
                </div>

                {/* TAB CONTROLS */}
                <div style={{ display: 'flex', gap: '20px', borderBottom: '1px solid #ddd', marginTop: '30px', marginBottom: '20px' }}>
                    <button
                        className={`tab-btn ${activeTab === 'products' ? 'active' : ''}`}
                        style={{ padding: '10px 20px', background: 'none', border: 'none', borderBottom: activeTab === 'products' ? '2px solid #c4161c' : 'none', fontWeight: activeTab === 'products' ? 'bold' : 'normal', cursor: 'pointer', color: activeTab === 'products' ? '#c4161c' : '#333' }}
                        onClick={() => setActiveTab('products')}
                    >
                        Sản phẩm ({products.length})
                    </button>

                    {isOwner && (
                        <button
                            className={`tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
                            style={{ padding: '10px 20px', background: 'none', border: 'none', borderBottom: activeTab === 'orders' ? '2px solid #c4161c' : 'none', fontWeight: activeTab === 'orders' ? 'bold' : 'normal', cursor: 'pointer', color: activeTab === 'orders' ? '#c4161c' : '#333' }}
                            onClick={() => setActiveTab('orders')}
                        >
                            Đơn hàng của Shop
                        </button>
                    )}
                </div>

                {/* CONTENT AREA */}
                {activeTab === 'products' ? (
                    <>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '20px' }}>
                            {isOwner && (
                                <button
                                    className="btn"
                                    onClick={() => navigate(`/create-product?shopId=${id}`)}
                                    style={{
                                        background: '#c4161c',
                                        color: 'white',
                                        border: 'none',
                                        padding: '10px 20px',
                                        fontSize: '16px',
                                        fontWeight: 'bold',
                                        cursor: 'pointer'
                                    }}
                                >
                                    ➕ Thêm Sản Phẩm
                                </button>
                            )}
                        </div>
                        <div className="product-grid">
                            {products.map(p => (
                                <div key={p.productId} className="product-card">
                                    <Link to={`/product/${p.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                        <img src={p.images?.[0]?.imageUrl || "https://placehold.co/300"} className="product-img" alt={p.name} />
                                        <h4 className="product-name">{p.name}</h4>
                                    </Link>
                                    <p className="product-price">{p.price.toLocaleString()} đ</p>
                                </div>
                            ))}
                        </div>
                    </>
                ) : (
                    /* ORDERS TAB CONTENT */
                    <div>
                        {orders.length === 0 ? (
                            <p style={{ textAlign: 'center', color: '#666', padding: '30px' }}>Chưa có đơn hàng nào.</p>
                        ) : (
                            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                                <thead>
                                    <tr style={{ background: '#f5f5f5', textAlign: 'left' }}>
                                        <th style={{ padding: '12px' }}>Mã Đơn</th>
                                        <th style={{ padding: '12px' }}>Ngày đặt</th>
                                        <th style={{ padding: '12px' }}>Sản phẩm</th>
                                        <th style={{ padding: '12px' }}>Tổng tiền</th>
                                        <th style={{ padding: '12px' }}>Trạng thái</th>
                                        <th style={{ padding: '12px' }}>Vận chuyển</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map(order => (
                                        <tr key={order.orderId || order.id} style={{ borderBottom: '1px solid #eee' }}>
                                            <td style={{ padding: '12px', fontFamily: 'monospace', fontWeight: 'bold' }}>
                                                #{order.orderId ? order.orderId.substring(0, 8) : 'N/A'}
                                                {order.cancellationRequested && order.status !== 'CANCELLED' && (
                                                    <div
                                                        onClick={() => {
                                                            const accept = window.confirm(`Khách hàng yêu cầu hủy đơn #${order.orderId.substring(0, 6)}. \n\nChọn OK để ĐỒNG Ý HỦY.\nChọn Cancel để TỪ CHỐI.`);
                                                            handleReplyCancel(order.orderId, accept);
                                                        }}
                                                        style={{
                                                            display: 'inline-block',
                                                            marginLeft: '5px',
                                                            cursor: 'pointer',
                                                            title: 'Khách yêu cầu hủy đơn'
                                                        }}
                                                    >
                                                        ⚠️ <span style={{ fontSize: '10px', color: 'red', fontWeight: 'bold' }}>Yêu cầu hủy</span>
                                                    </div>
                                                )}
                                            </td>
                                            <td style={{ padding: '12px', fontSize: '13px' }}>
                                                {order.createdAt ? new Date(order.createdAt).toLocaleDateString('vi-VN') : new Date().toLocaleDateString('vi-VN')} <br />
                                                <span style={{ color: '#888', fontSize: '12px' }}>{order.createdAt ? new Date(order.createdAt).toLocaleTimeString('vi-VN') : ''}</span>
                                            </td>
                                            <td style={{ padding: '12px' }}>
                                                {order.items?.map((item, idx) => (
                                                    <div key={idx} style={{ marginBottom: '8px' }}>
                                                        <Link to={`/product/${item.productId}`} style={{ textDecoration: 'none', color: '#333', display: 'block' }} onMouseEnter={e => e.target.style.color = '#c4161c'} onMouseLeave={e => e.target.style.color = '#333'}>
                                                            • <b>{item.productName || 'Sản phẩm'}</b> <span style={{ color: '#666' }}>(x{item.quantity})</span>
                                                        </Link>
                                                    </div>
                                                ))}
                                            </td>
                                            <td style={{ padding: '12px', fontWeight: 'bold', color: '#c4161c' }}>{order.totalAmount?.toLocaleString()} đ</td>
                                            <td style={{ padding: '12px' }}>
                                                <span style={{
                                                    padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: '600',
                                                    background: (order.cancellationRequested) ? '#fff3e0' : (order.status === 'PENDING' || !order.status) ? '#e2e3e5' : order.status === 'CANCELLED' ? '#e2e3e5' : order.status === 'COMPLETED' ? '#d1e7dd' : '#f8d7da',
                                                    color: (order.cancellationRequested) ? '#e65100' : (order.status === 'PENDING' || !order.status) ? '#383d41' : order.status === 'CANCELLED' ? '#6c757d' : order.status === 'COMPLETED' ? '#0f5132' : '#842029'
                                                }}>
                                                    {order.cancellationRequested ? 'Yêu cầu hủy' : (!order.status || order.status === 'PENDING') ? 'Đang xử lý' : order.status}
                                                </span>
                                            </td>
                                            <td style={{ padding: '12px' }}>
                                                <div>
                                                    <div style={{ fontWeight: 'bold', color: '#0d6efd', fontSize: '13px' }}>GHN Express</div>
                                                    <div style={{ fontSize: '12px', color: '#666' }}>
                                                        Ship: {order.shipment ? (order.shipment.shippingFee !== undefined && order.shipment.shippingFee !== null ? order.shipment.shippingFee.toLocaleString() + 'đ' : '...') : '30.000đ'}
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                )}
            </main>
            <Footer />
        </div>
    );
};

export default ShopDetail;
