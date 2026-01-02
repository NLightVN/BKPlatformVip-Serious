import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { shopService } from '../services/shopService';
import { productService } from '../services/productService';
import { orderService } from '../services/orderService';
import { AuthContext } from '../context/AuthContext';
import { adminService } from '../services/adminService';
import { toast } from 'react-toastify';

const ShopDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user, isAdmin } = useContext(AuthContext);
    const [shop, setShop] = useState(null);
    const [products, setProducts] = useState([]); // Khôi phục products
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true); // Khôi phục loading
    const [activeTab, setActiveTab] = useState('products'); // products | orders

    const isOwner = user && shop && (user.username === shop.ownerUsername || isAdmin());
    const isRealOwner = user && shop && user.username === shop.ownerUsername; // Only real owner, not admin

    useEffect(() => {
        // 1. Lấy thông tin Shop
        shopService.getShopById(id).then(res => {
            const data = res.result || res;
            setShop(data);
        });

        // 2. Lấy sản phẩm
        productService.getProductsByShop(id).then(res => {
            if (res.code === 1000) {
                // Filter out deleted products for non-admin/non-owner
                const productList = res.result || [];
                const filteredProducts = (isAdmin() || isOwner) ? productList : productList.filter(p => p.status !== 'DELETED');
                setProducts(filteredProducts);
            }
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

    const handleConfirmOrder = async (orderId) => {
        if (!window.confirm("Xác nhận đơn hàng này và chuyển sang Chờ lấy hàng?")) return;
        try {
            const res = await orderService.confirmOrder(orderId);
            if (res.code === 1000) {
                toast.success("Đã xác nhận đơn hàng!");
                // Refresh list
                const updatedOrders = orders.map(o => {
                    if (o.orderId === orderId) {
                        return { ...o, status: 'AWAITING_PICKUP' };
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

    const handleDeleteShop = async () => {
        if (!window.confirm(`CẢNH BÁO: Xóa shop sẽ xóa tất cả sản phẩm!\n\nBạn có chắc chắn muốn xóa shop "${shop.name}"?`)) return;
        try {
            const res = await shopService.deleteShop(id);
            if (res.code === 1000) {
                toast.success("Đã xóa shop thành công!");
                setTimeout(() => navigate('/seller-channel'), 1500);
            } else {
                toast.error(res.message || "Lỗi khi xóa shop");
            }
        } catch (err) {
            toast.error("Lỗi hệ thống");
        }
    };

    const handleDeleteProductFromCard = async (productId, productName) => {
        if (!window.confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${productName}"?\n\nSản phẩm sẽ không còn hiển thị trong marketplace nhưng vẫn giữ lại trong lịch sử đơn hàng.`)) {
            return;
        }

        try {
            await productService.deleteProduct(productId);
            toast.success("Đã xóa sản phẩm thành công!");
            // Refresh products list
            setProducts(prevProducts => prevProducts.filter(p => p.productId !== productId));
        } catch (err) {
            console.error(err);
            toast.error("Lỗi khi xóa sản phẩm");
        }
    };

    const handleBanShop = async () => {
        try {
            const banned = shop.status === 'BANNED';
            await adminService.banShop(shop.shopId, !banned);
            toast.success(banned ? 'Đã gỡ ban shop' : 'Đã ban shop');
            // Reload shop
            const res = await shopService.getShopById(id);
            setShop(res.result || res);
        } catch (error) {
            toast.error('Lỗi khi thay đổi trạng thái ban');
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
                        <h1 style={{ marginBottom: '5px' }}>
                            {shop.name}
                            {shop.status === 'DELETED' && <span style={{ color: '#ff6b6b', fontSize: '14px', marginLeft: '10px', fontWeight: 'normal' }}>(Đã đóng cửa)</span>}
                            {shop.status === 'BANNED' && <span style={{ color: '#ff0000', fontSize: '14px', marginLeft: '10px', fontWeight: 'normal' }}>⛔ (BANNED)</span>}
                        </h1>
                        <p style={{ color: '#666' }}>Chủ sở hữu: <strong>{shop.ownerUsername}</strong></p>
                        <p style={{ color: '#666' }}>
                            📍 {shop.address?.addressDetail}, {shop.address?.ward?.fullName}, {shop.address?.district?.fullName}
                        </p>

                        {/* Shop Status Warning */}
                        {(shop.status === 'DELETED' || shop.status === 'BANNED') && (
                            <div style={{ background: shop.status === 'BANNED' ? '#ffebee' : '#fff3cd', border: '1px solid ' + (shop.status === 'BANNED' ? '#ffcdd2' : '#ffecb5'), borderRadius: '8px', padding: '12px', marginTop: '10px', color: shop.status === 'BANNED' ? '#c62828' : '#856404', fontSize: '14px' }}>
                                {shop.status === 'BANNED' ? '⛔' : '⚠️'} <strong>{shop.status === 'BANNED' ? 'Shop đã bị ban' : 'Shop đã đóng cửa'}</strong> - Không thể mua sản phẩm từ shop này
                            </div>
                        )}
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

                    {/* Only real owner can see revenue, not admin */}
                    {isRealOwner && (
                        <button
                            onClick={() => navigate(`/shop/${id}/revenue`)}
                            style={{
                                padding: '10px 20px',
                                background: '#28a745',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontWeight: '600',
                                fontSize: '14px',
                                marginLeft: '10px'
                            }}
                        >
                            📊 Doanh thu
                        </button>
                    )}

                    <div style={{ flex: 1 }} />

                    {/* Only real owner can delete shop, not admin */}
                    {isRealOwner && (
                        <button
                            onClick={handleDeleteShop}
                            style={{
                                background: '#dc3545',
                                color: 'white',
                                border: 'none',
                                padding: '8px 16px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '600',
                                alignSelf: 'center'
                            }}
                        >
                            🗑️ Xóa Shop
                        </button>
                    )}

                    {/* Admin ban shop button */}
                    {user && isAdmin() && (
                        <button
                            onClick={handleBanShop}
                            style={{
                                background: shop.status === 'BANNED' ? '#4CAF50' : '#ff9800',
                                color: 'white',
                                border: 'none',
                                padding: '8px 16px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '600',
                                alignSelf: 'center',
                                marginLeft: '10px'
                            }}
                        >
                            {shop.status === 'BANNED' ? '✅ Gỡ Ban Shop' : '⛔ Ban Shop'}
                        </button>
                    )}
                </div>

                {/* CONTENT AREA */}
                {activeTab === 'products' ? (
                    <>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '20px' }}>
                            {/* Only real owner can add products, not admin */}
                            {isRealOwner && (
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
                                <div key={p.productId} className="product-card" style={{ position: 'relative' }}>
                                    <Link to={`/product/${p.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                        <img src={p.images?.[0]?.imageUrl || "https://placehold.co/300"} className="product-img" alt={p.name} />
                                        <h4 className="product-name">{p.name}</h4>
                                    </Link>
                                    <p className="product-price">{p.price.toLocaleString()} đ</p>

                                    {/* Edit and Delete buttons only for real owner, not admin */}
                                    {isRealOwner && (
                                        <div style={{
                                            position: 'absolute',
                                            top: '8px',
                                            right: '8px',
                                            display: 'flex',
                                            gap: '6px'
                                        }}>
                                            <button
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    navigate(`/update-product/${p.productId}`);
                                                }}
                                                style={{
                                                    background: 'rgba(76, 175, 80, 0.9)',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '50%',
                                                    width: '32px',
                                                    height: '32px',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'center',
                                                    fontSize: '16px',
                                                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                                                }}
                                                title="Chỉnh sửa sản phẩm"
                                            >
                                                ✏️
                                            </button>
                                            <button
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    handleDeleteProductFromCard(p.productId, p.name);
                                                }}
                                                style={{
                                                    background: 'rgba(244, 67, 54, 0.9)',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '50%',
                                                    width: '32px',
                                                    height: '32px',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'center',
                                                    fontSize: '16px',
                                                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                                                }}
                                                title="Xóa sản phẩm"
                                            >
                                                🗑️
                                            </button>
                                        </div>
                                    )}
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
                                        {/* Only show Actions column for real owner, not admin */}
                                        {isRealOwner && <th style={{ padding: '12px' }}>Thao tác</th>}
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
                                                {order.items?.map((item, idx) => {
                                                    const isDeleted = item.productDeleted;
                                                    const productName = item.productName || item.product?.name || 'Sản phẩm';

                                                    return (
                                                        <div key={idx} style={{ marginBottom: '8px' }}>
                                                            {isDeleted ? (
                                                                // Deleted product - no link
                                                                <div>
                                                                    <span style={{ color: '#666' }}>• <b>{productName}</b></span> <span style={{ color: '#666' }}>(x{item.quantity})</span>
                                                                    <div style={{ fontSize: '11px', color: '#ff6b6b', fontStyle: 'italic', marginLeft: '10px' }}>
                                                                        (không còn tồn tại)
                                                                    </div>
                                                                </div>
                                                            ) : (
                                                                // Active product - clickable link
                                                                <Link
                                                                    to={`/product/${item.productId}`}
                                                                    style={{ textDecoration: 'none', color: '#333', display: 'block' }}
                                                                    onMouseEnter={e => e.target.style.color = '#c4161c'}
                                                                    onMouseLeave={e => e.target.style.color = '#333'}
                                                                >
                                                                    • <b>{productName}</b> <span style={{ color: '#666' }}>(x{item.quantity})</span>
                                                                </Link>
                                                            )}
                                                        </div>
                                                    );
                                                })}
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
                                            {/* Only show Actions column for real owner, not admin */}
                                            {isRealOwner && (
                                                <td style={{ padding: '12px' }}>
                                                    {(() => {
                                                        // Check if order has deleted products
                                                        const hasDeletedProducts = order.items?.some(item => item.productDeleted);

                                                        if (order.status === 'PENDING' && !order.cancellationRequested && !hasDeletedProducts) {
                                                            return (
                                                                <button
                                                                    onClick={() => handleConfirmOrder(order.orderId)}
                                                                    style={{
                                                                        background: '#28a745',
                                                                        color: 'white',
                                                                        border: 'none',
                                                                        padding: '6px 12px',
                                                                        borderRadius: '4px',
                                                                        cursor: 'pointer',
                                                                        fontSize: '12px',
                                                                        fontWeight: '600'
                                                                    }}
                                                                >
                                                                    ✓ Xác nhận
                                                                </button>
                                                            );
                                                        } else if (hasDeletedProducts) {
                                                            return (
                                                                <span style={{ fontSize: '11px', color: '#999', fontStyle: 'italic' }}>
                                                                    (Có sản phẩm đã xóa)
                                                                </span>
                                                            );
                                                        }
                                                        return null;
                                                    })()}
                                                </td>
                                            )}
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
