import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { productService } from '../services/productService';
import { shopService } from '../services/shopService'; // Import thêm shopService
import { ShoppingCartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const ProductDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [shop, setShop] = useState(null); // State lưu thông tin Shop
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);
    
    const { addToCart } = useContext(ShoppingCartContext);
    const { user } = useContext(AuthContext);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Lấy thông tin sản phẩm
                const productRes = await productService.getProductById(id);
                if (productRes.code === 1000) {
                    const productData = productRes.result;
                    setProduct(productData);

                    // 2. Nếu có sản phẩm, lấy tiếp thông tin Shop dựa trên shopId
                    if (productData.shopId) {
                        const shopRes = await shopService.getShopById(productData.shopId);
                        // Backend có thể trả về trực tiếp object hoặc bọc trong result
                        const shopData = shopRes.result || shopRes; 
                        setShop(shopData);
                    }
                }
            } catch (err) {
                toast.error("Không tải được thông tin sản phẩm");
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    const handleQuantityChange = (amount) => {
        const newQty = quantity + amount;
        if (newQty >= 1) setQuantity(newQty);
    };

    const handleAddToCart = () => {
        if (!user) {
            toast.info("Vui lòng đăng nhập để mua hàng");
            navigate('/login');
            return;
        }
        if(product) {
            addToCart(product.productId, quantity);
            toast.success("Đã thêm vào giỏ hàng");
        }
    };

    const handleBuyNow = () => {
        if (!user) {
            toast.info("Vui lòng đăng nhập để mua hàng");
            navigate('/login');
            return;
        }
        if(product) {
            navigate('/checkout', { 
                state: { 
                    items: [{
                        productId: product.productId,
                        productName: product.name,
                        price: product.price,
                        quantity: quantity,
                        shopId: product.shopId,
                        weight: product.weight || 200
                    }] 
                } 
            });
        }
    };

    if (loading) return <div className="app-container"><Header /><main className="main-content">Đang tải...</main></div>;
    if (!product) return <div className="app-container"><Header /><main className="main-content">Sản phẩm không tồn tại</main></div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{display: 'block', padding: '40px'}}>
                <div style={{maxWidth: '1200px', margin: '0 auto'}}>
                    <Link to="/shopping" style={{textDecoration: 'none', color: '#666', marginBottom: '20px', display: 'inline-block'}}>
                        &larr; Quay lại mua sắm
                    </Link>

                    <div style={{display: 'flex', gap: '40px', background: 'white', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)'}}>
                        {/* Cột trái: Ảnh */}
                        <div style={{flex: 1}}>
                            <img 
                                src={product.images && product.images.length > 0 ? product.images[0].imageUrl : "https://placehold.co/400"} 
                                alt={product.name} 
                                style={{width: '100%', borderRadius: '10px', objectFit: 'cover', border: '1px solid #eee'}} 
                            />
                            <div style={{display:'flex', gap:'10px', marginTop:'10px', overflowX:'auto'}}>
                                {product.images?.map((img, idx) => (
                                    <img key={idx} src={img.imageUrl} style={{width:'60px', height:'60px', borderRadius:'8px', cursor:'pointer', border:'1px solid #ddd'}} />
                                ))}
                            </div>
                        </div>

                        {/* Cột phải: Thông tin */}
                        <div style={{flex: 1.2}}>
                            <h1 style={{fontSize: '32px', marginBottom: '10px'}}>{product.name}</h1>
                            
                            {/* --- THÔNG TIN SHOP --- */}
                            <div style={{display:'flex', alignItems:'center', gap:'10px', marginBottom:'20px', padding:'10px', background:'#f9f9f9', borderRadius:'8px'}}>
                                <div style={{width:'40px', height:'40px', background:'#c4161c', color:'white', borderRadius:'50%', display:'flex', alignItems:'center', justifyContent:'center', fontWeight:'bold'}}>
                                    {shop ? shop.name.charAt(0).toUpperCase() : 'S'}
                                </div>
                                <div>
                                    <div style={{fontSize:'14px', color:'#666'}}>Được bán bởi:</div>
                                    {shop ? (
                                        <Link to={`/shop/${shop.shopId}`} style={{color:'#c4161c', fontWeight:'bold', textDecoration:'none'}}>
                                            {shop.name} &rarr;
                                        </Link>
                                    ) : (
                                        <span>Đang tải thông tin shop...</span>
                                    )}
                                </div>
                            </div>
                            
                            <div style={{fontSize: '32px', color: '#c4161c', fontWeight: 'bold', margin: '20px 0'}}>
                                {product.price.toLocaleString()} đ
                            </div>

                            <div style={{marginBottom: '20px', display:'flex', alignItems:'center', gap:'10px'}}>
                                <span>Số lượng:</span>
                                <div style={{display:'flex', alignItems:'center', border:'1px solid #ccc', borderRadius:'5px'}}>
                                    <button onClick={() => handleQuantityChange(-1)} style={{padding:'5px 10px', border:'none', background:'none', cursor:'pointer'}}>-</button>
                                    <span style={{padding:'0 10px', borderLeft:'1px solid #ccc', borderRight:'1px solid #ccc'}}>{quantity}</span>
                                    <button onClick={() => handleQuantityChange(1)} style={{padding:'5px 10px', border:'none', background:'none', cursor:'pointer'}}>+</button>
                                </div>
                            </div>

                            <div style={{marginBottom: '30px', lineHeight: '1.6', color: '#444'}}>
                                <strong>Mô tả sản phẩm:</strong><br/>
                                {product.description || "Chưa có mô tả chi tiết."}
                            </div>

                            <div style={{display:'flex', gap:'15px'}}>
                                <button className="login-btn" style={{flex: 1, fontSize: '16px', padding:'12px'}} onClick={handleBuyNow}>
                                    Mua ngay
                                </button>
                                <button className="btn" style={{flex: 1, fontSize: '16px', padding:'12px'}} onClick={handleAddToCart}>
                                    Thêm vào giỏ
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default ProductDetail;