import React, { useContext, useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { ShoppingCartContext } from '../context/CartContext';

const Cart = () => {
    const { cart, removeFromCart, updateQuantity } = useContext(ShoppingCartContext);

    // Local state for immediate quantity display (Shopee-style)
    const [localQuantities, setLocalQuantities] = useState({});
    const debounceTimers = useRef({});

    // Sync local quantities with cart
    useEffect(() => {
        if (cart?.items) {
            const quantities = {};
            cart.items.forEach(item => {
                quantities[item.productId] = item.quantity;
            });
            setLocalQuantities(quantities);
        }
    }, [cart?.items]);

    // Debounced update to backend
    const debouncedUpdate = (productId, newQuantity) => {
        // Clear existing timer
        if (debounceTimers.current[productId]) {
            clearTimeout(debounceTimers.current[productId]);
        }

        // Set new timer (500ms debounce)
        debounceTimers.current[productId] = setTimeout(() => {
            updateQuantity(productId, newQuantity);
        }, 500);
    };

    const handleQuantityChange = (productId, delta) => {
        const currentQty = localQuantities[productId] || 1;
        const newQty = Math.max(1, currentQty + delta);

        // Update local state immediately (instant UI feedback)
        setLocalQuantities(prev => ({
            ...prev,
            [productId]: newQty
        }));

        // Call API with debounce
        debouncedUpdate(productId, newQty);
    };

    const handleInputChange = (productId, value) => {
        const num = parseInt(value) || 1;
        const newQty = Math.max(1, num);

        setLocalQuantities(prev => ({
            ...prev,
            [productId]: newQty
        }));

        debouncedUpdate(productId, newQty);
    };

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ flexDirection: 'column', gap: '20px', padding: '40px' }}>
                <h2>Giỏ hàng của bạn</h2>
                <div style={{ display: 'flex', gap: '20px', width: '100%', maxWidth: '1000px', margin: '0 auto' }}>
                    <div style={{ flex: 2, background: 'white', borderRadius: '10px', padding: '20px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                        {cart && cart.items && cart.items.length > 0 ? (
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                    <tr style={{ textAlign: 'left', borderBottom: '2px solid #f0f0f0' }}>
                                        <th style={{ padding: '10px' }}>Sản phẩm</th>
                                        <th>Đơn giá</th>
                                        <th>Số lượng</th>
                                        <th>Thành tiền</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {cart.items.map(item => (
                                        <tr key={item.productId} style={{ borderBottom: '1px solid #eee' }}>
                                            <td style={{ padding: '15px 10px' }}>
                                                <strong>{item.product ? item.product.name : 'Sản phẩm không còn tồn tại'}</strong>
                                                {item.product?.deleted && (
                                                    <div style={{ color: '#dc3545', fontSize: '12px', marginTop: '4px' }}>
                                                        ⚠️ Sản phẩm đã ngừng bán
                                                    </div>
                                                )}
                                                {!item.product && (
                                                    <div style={{ color: '#999', fontSize: '12px', marginTop: '4px' }}>
                                                        Sản phẩm này không còn tồn tại
                                                    </div>
                                                )}
                                            </td>
                                            <td style={{ color: '#666' }}>
                                                {(item.price || 0).toLocaleString()} đ
                                            </td>
                                            <td>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                    <button
                                                        className="qty-btn"
                                                        onClick={() => handleQuantityChange(item.productId, -1)}
                                                        disabled={(localQuantities[item.productId] || item.quantity) <= 1}
                                                        style={{
                                                            width: '28px',
                                                            height: '28px',
                                                            border: '1px solid #ddd',
                                                            background: 'white',
                                                            cursor: (localQuantities[item.productId] || item.quantity) <= 1 ? 'not-allowed' : 'pointer',
                                                            borderRadius: '2px',
                                                            fontSize: '16px',
                                                            opacity: (localQuantities[item.productId] || item.quantity) <= 1 ? 0.5 : 1
                                                        }}
                                                    >
                                                        -
                                                    </button>
                                                    <input
                                                        type="number"
                                                        min="1"
                                                        value={localQuantities[item.productId] || item.quantity}
                                                        onChange={(e) => handleInputChange(item.productId, e.target.value)}
                                                        style={{
                                                            width: '50px',
                                                            height: '28px',
                                                            textAlign: 'center',
                                                            border: '1px solid #ddd',
                                                            borderRadius: '2px',
                                                            fontSize: '14px'
                                                        }}
                                                    />
                                                    <button
                                                        className="qty-btn"
                                                        onClick={() => handleQuantityChange(item.productId, 1)}
                                                        style={{
                                                            width: '28px',
                                                            height: '28px',
                                                            border: '1px solid #ddd',
                                                            background: 'white',
                                                            cursor: 'pointer',
                                                            borderRadius: '2px',
                                                            fontSize: '16px'
                                                        }}
                                                    >
                                                        +
                                                    </button>
                                                </div>
                                            </td>
                                            <td style={{ color: '#c4161c', fontWeight: 'bold' }}>
                                                {((item.price || 0) * (localQuantities[item.productId] || item.quantity)).toLocaleString()} đ
                                            </td>
                                            <td>
                                                <button onClick={() => removeFromCart(item.productId)} style={{ color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontSize: '20px' }}>×</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p style={{ textAlign: 'center', padding: '20px' }}>Giỏ hàng trống. <Link to="/shopping" style={{ color: '#c4161c' }}>Mua sắm ngay</Link></p>
                        )}
                    </div>

                    <div style={{ flex: 1, height: 'fit-content', background: 'white', borderRadius: '10px', padding: '20px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                        <h3>Tổng cộng</h3>
                        <div style={{ display: 'flex', justifyContent: 'space-between', margin: '20px 0', fontSize: '18px', fontWeight: 'bold' }}>
                            <span>Tạm tính:</span>
                            <span style={{ color: '#c4161c' }}>{cart ? cart.totalAmount.toLocaleString() : 0} đ</span>
                        </div>
                        {cart && cart.items.length > 0 && (
                            <Link to="/checkout">
                                <button className="login-btn" style={{ width: '100%' }}>Tiến hành thanh toán</button>
                            </Link>
                        )}
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default Cart;