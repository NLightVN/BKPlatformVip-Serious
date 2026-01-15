import React, { useState, useEffect, useContext } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { productService } from '../services/productService';
import { ShoppingCartContext } from '../context/CartContext';
import { toast } from 'react-toastify';

const SearchPage = () => {
const [searchParams] = useSearchParams();
const query = searchParams.get('q');
const [products, setProducts] = useState([]);
const { addToCart } = useContext(ShoppingCartContext);


useEffect(() => {
    if(query) {
        productService.searchProducts(query).then(res => {
            if(res.code === 1000) setProducts(res.result);
        });
    }
}, [query]);

const handleAddToCart = (e, productId) => {
    e.preventDefault();
    addToCart(productId, 1);
    toast.success("Đã thêm vào giỏ hàng");
};

return (
    <div className="app-container">
        <Header />
        <main className="main-content" style={{display:'block', padding:'40px'}}>
            <h2>Kết quả tìm kiếm cho: "{query}"</h2>
            {products.length === 0 ? <p>Không tìm thấy sản phẩm nào.</p> : (
                <div className="product-grid" style={{
                    marginTop:'20px',
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
                    gap: '20px'
                }}>
                    {products.map((product) => (
                        <div key={product.productId} className="product-card">
                            <Link to={`/product/${product.productId}`} style={{textDecoration:'none', color:'inherit'}}>
                                <img 
                                    src={product.images && product.images.length > 0 ? product.images[0].imageUrl : "https://placehold.co/300"} 
                                    alt={product.name} 
                                    className="product-img" 
                                />
                                <h4 className="product-name">{product.name}</h4>
                            </Link>
                            <p className="product-price">{product.price.toLocaleString()} đ</p>
                            <button className="add-cart-btn" onClick={(e) => handleAddToCart(e, product.productId)}>
                                Thêm vào giỏ
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </main>
        <Footer />
    </div>
);

};

export default SearchPage;