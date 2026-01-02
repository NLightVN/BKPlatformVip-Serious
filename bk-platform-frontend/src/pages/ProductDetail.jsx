import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { productService } from '../services/productService';
import { shopService } from '../services/shopService';
import { ShoppingCartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const ProductDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [shop, setShop] = useState(null);
    const [loading, setLoading] = useState(true);

    // Modal State
    const [showBuyModal, setShowBuyModal] = useState(false);
    const [buyQuantity, setBuyQuantity] = useState(1);

    const { addToCart } = useContext(ShoppingCartContext);
    const { user } = useContext(AuthContext);

    useEffect(() => {
        productService.getProductById(id).then(res => {
            if (res.code === 1000) {
                setProduct(res.result);
                if (res.result.shopId) {
                    shopService.getShopById(res.result.shopId).then(sRes => {
                        setShop(sRes.result || sRes); // Xử lý tùy format backend
                    });
                }
            }
        }).finally(() => setLoading(false));
    }, [id]);

    const handleAddToCart = () => {
        if (!user) return navigate('/login');
        if (product) {
            addToCart(product.productId, 1);
            toast.success("Đã thêm vào giỏ hàng");
        }
    };

    const handleOpenBuyModal = () => {
        if (!user) return navigate('/login');
        setShowBuyModal(true);
    };

    const handleConfirmBuyNow = () => {
        // Chuyển sang Checkout với số lượng đã chọn
        navigate('/checkout', {
            state: {
                items: [{
                    productId: product.productId,
                    productName: product.name,
                    price: product.price,
                    quantity: buyQuantity,
                    shopId: product.shopId,
                    weight: product.weight || 200
                }]
            }
        });
    };

    if (loading) return <div className="app-container"><Header /><main className="main-content">Đang tải...</main></div>;
    if (!product) return <div className="app-container"><Header /><main className="main-content">Sản phẩm không tồn tại</main></div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                <Link to="/shopping" style={{ textDecoration: 'none', color: '#666', marginBottom: '20px', display: 'inline-block' }}>&larr; Quay lại</Link>

                <div style={{ display: 'flex', gap: '40px', background: 'white', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                    <div style={{ flex: 1 }}>
                        <img src={product.images?.[0]?.imageUrl || "https://placehold.co/400"} alt={product.name} style={{ width: '100%', borderRadius: '10px' }} />
                    </div>

                    <div style={{ flex: 1.2 }}>
                        <h1 style={{ fontSize: '32px', marginBottom: '10px' }}>{product.name}</h1>

                        {/* Shop Info */}
                        <div style={{ padding: '10px', background: '#f8f9fa', borderRadius: '8px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <div style={{ width: '40px', height: '40px', background: '#c4161c', borderRadius: '50%', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
                                {shop ? shop.name.charAt(0) : 'S'}
                            </div>
                            <div>
                                <span style={{ fontSize: '13px', color: '#666' }}>Cung cấp bởi:</span><br />
                                {shop ? <Link to={`/shop/${shop.shopId}`} style={{ fontWeight: 'bold', color: '#333' }}>{shop.name} &rarr;</Link> : 'Đang tải...'}
                            </div>
                        </div>

                        <div style={{ fontSize: '32px', color: '#c4161c', fontWeight: 'bold', marginBottom: '20px' }}>{product.price.toLocaleString()} đ</div>
                        <div style={{ marginBottom: '30px', lineHeight: '1.6' }}>{product.description}</div>

                        <div style={{ display: 'flex', gap: '15px' }}>
                            <button className="login-btn" style={{ flex: 1, padding: '12px' }} onClick={handleOpenBuyModal}>Mua ngay</button>
                            <button className="btn" style={{ flex: 1, padding: '12px' }} onClick={handleAddToCart}>Thêm vào giỏ</button>
                        </div>

                        {/* Edit button for shop owner */}
                        {user && shop && user.username === shop.ownerUsername && (
                            <button
                                className="btn"
                                onClick={() => navigate(`/update-product/${product.productId}`)}
                                style={{
                                    width: '100%',
                                    marginTop: '15px',
                                    background: '#4CAF50',
                                    color: 'white',
                                    border: 'none',
                                    padding: '12px',
                                    fontWeight: 'bold'
                                }}
                            >
                                ✏️ Chỉnh Sửa Sản Phẩm
                            </button>
                        )}
                    </div>
                </div>

                {/* MODAL MUA NGAY */}
                {showBuyModal && (
                    <div className="modal-overlay" onClick={() => setShowBuyModal(false)}>
                        <div className="modal-content" onClick={e => e.stopPropagation()}>
                            <h3 style={{ marginBottom: '15px' }}>Chọn số lượng</h3>
                            <div style={{ display: 'flex', gap: '20px', alignItems: 'center', marginBottom: '20px' }}>
                                <img src={product.images?.[0]?.imageUrl || "https://placehold.co/100"} style={{ width: '60px', height: '60px', borderRadius: '8px' }} />
                                <div>
                                    <div style={{ fontWeight: 'bold' }}>{product.name}</div>
                                    <div style={{ color: '#c4161c' }}>{product.price.toLocaleString()} đ</div>
                                </div>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '30px' }}>
                                <span>Số lượng:</span>
                                <div className="quantity-control">
                                    <button className="qty-btn" onClick={() => setBuyQuantity(q => Math.max(1, q - 1))}>-</button>
                                    <span className="qty-val">{buyQuantity}</span>
                                    <button className="qty-btn" onClick={() => setBuyQuantity(q => q + 1)}>+</button>
                                </div>
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 'bold', marginBottom: '20px', fontSize: '18px' }}>
                                <span>Tạm tính:</span>
                                <span style={{ color: '#c4161c' }}>{(product.price * buyQuantity).toLocaleString()} đ</span>
                            </div>

                            <button className="login-btn" style={{ width: '100%' }} onClick={handleConfirmBuyNow}>Xác nhận mua</button>
                        </div>
                    </div>
                )}
            </main>
            <Footer />
        </div>
    );
};

export default ProductDetail;