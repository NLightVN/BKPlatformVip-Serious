import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import { productService } from './services/productService';
import { ShoppingCartContext } from './context/CartContext';
import { toast } from 'react-toastify';

const Shopping = () => {
const [products, setProducts] = useState([]);
const [loading, setLoading] = useState(true);
const { addToCart } = useContext(ShoppingCartContext);

useEffect(() => {
// Gọi API lấy sản phẩm thật
productService.getAllProducts()
.then(res => {
if(res.code === 1000) setProducts(res.result);
})
.catch(err => console.error(err))
.finally(() => setLoading(false));
}, []);

const handleAddToCart = (product) => {
addToCart(product.productId, 1);
toast.success(Đã thêm ${product.name} vào giỏ);
};

if (loading) return <div>Loading products...</div>;

return (
<div className="app-container">
<Header />


<main className="main-content" style={{display:'block', padding:'20px'}}>
    <div style={{display: 'flex', gap: '20px'}}>
      
      <div className="sidebar">
        <h3>Shop by categories</h3>
        {/* Nên gọi API Category để render động */}
        {['Clothes', 'Study Materials', 'Tools', 'Services'].map(cat => (
          <button key={cat} className="cat-btn">{cat}</button>
        ))}
      </div>

      <div style={{flexGrow: 1}}>
         {/* ... Sort logic giữ nguyên nếu muốn client-side sort ... */}
        
        <div className="product-grid">
          {products.map((product) => (
            <div key={product.productId} className="product-card">
              <Link to={`/product/${product.productId}`} style={{textDecoration:'none', color:'inherit'}}>
                {/* Sử dụng ảnh đầu tiên hoặc ảnh placeholder */}
                <img 
                    src={product.images && product.images.length > 0 ? product.images[0].imageUrl : "https://placehold.co/150"} 
                    alt={product.name} 
                    className="product-img" 
                />
                <h4 className="product-name">{product.name}</h4>
              </Link>
              <p className="product-price">{product.price.toLocaleString()} đ</p>
              <button className="add-cart-btn" onClick={() => handleAddToCart(product)}>
                Add to cart
              </button>
            </div>
          ))}
        </div>
        
      </div>
    </div>
  </main>
  <Footer />
</div>

);
};

export default Shopping;