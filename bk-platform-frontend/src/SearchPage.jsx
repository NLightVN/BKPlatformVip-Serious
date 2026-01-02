import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import { productService } from './services/productService';

const SearchPage = () => {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get('q') || '';
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (keyword) {
      productService.searchProducts(keyword)
        .then(res => {
          if (res.code === 1000) setProducts(res.result);
        })
        .finally(() => setLoading(false));
    }
  }, [keyword]);

  return (
    <>
      <Header />
      <div style={{ padding: '40px 20px', maxWidth: '1200px', margin: '0 auto' }}>
        <h2>Kết quả tìm kiếm cho: "{keyword}"</h2>
        {loading ? (
          <p>Đang tìm kiếm...</p>
        ) : products.length > 0 ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '20px', marginTop: '20px' }}>
            {products.map(p => (
              <div key={p.productId} style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '8px' }}>
                <h3>{p.name}</h3>
                <p>{p.price.toLocaleString()} đ</p>
              </div>
            ))}
          </div>
        ) : (
          <p>Không tìm thấy sản phẩm nào</p>
        )}
      </div>
      <Footer />
    </>
  );
};

export default SearchPage;