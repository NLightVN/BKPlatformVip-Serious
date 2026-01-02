import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { ShoppingCartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const Shopping = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sortOption, setSortOption] = useState('default');
    const [categories, setCategories] = useState([]); // Categories from database
    const { addToCart } = useContext(ShoppingCartContext);
    const { isAdmin } = useContext(AuthContext);

    // Helper to capitalize first letter of each word
    const capitalizeWords = (str) => {
        if (!str) return '';
        return str.split(' ').map(word =>
            word.charAt(0).toUpperCase() + word.slice(1)
        ).join(' ');
    };

    useEffect(() => {
        // Load products
        productService.getAllProducts().then(res => {
            if (res.code === 1000) {
                setProducts(res.result);
            }
        }).finally(() => setLoading(false));

        // Load categories from database
        categoryService.getAllCategories().then(res => {
            if (res.code === 1000 && res.result) {
                setCategories(res.result.map(cat => cat.name));
            }
        }).catch(err => {
            console.error('Could not load categories:', err);
        });
    }, []);

    // Helper function to get abbreviation from product name
    const getAbbr = (name) => {
        if (!name) return '?';
        const words = name.split(' ');
        if (words.length === 1) return name.substring(0, 4).toUpperCase();
        return words.slice(0, Math.min(2, words.length))
            .map(w => w.charAt(0))
            .join('')
            .toUpperCase();
    };

    // Filter products by search and category
    const filteredProducts = products.filter(product => {
        const matchesSearch = product.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            product.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            product.brand?.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesCategory = selectedCategory === 'all' ||
            product.categories?.some(cat => cat.name === selectedCategory);
        return matchesSearch && matchesCategory;
    });

    // Sort products
    const sortedProducts = [...filteredProducts].sort((a, b) => {
        if (sortOption === 'price-low') return (a.price || 0) - (b.price || 0);
        if (sortOption === 'price-high') return (b.price || 0) - (a.price || 0);
        if (sortOption === 'name') return (a.name || '').localeCompare(b.name || '');
        return 0; // default
    });

    return (
        <div className="app-container">
            <Header />

            <div className="container">
                {/* SIDEBAR */}
                <aside className="sidebar">
                    <h3>Danh mục</h3>
                    <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                        <div
                            className={`category ${selectedCategory === 'all' ? 'active' : ''}`}
                            onClick={() => setSelectedCategory('all')}
                        >
                            Tất cả
                        </div>
                        {categories.map(cat => (
                            <div
                                key={cat}
                                className={`category ${selectedCategory === cat ? 'active' : ''}`}
                                onClick={() => setSelectedCategory(cat)}
                            >
                                {capitalizeWords(cat)}
                            </div>
                        ))}
                    </div>
                </aside>

                {/* MAIN */}
                <section>
                    {/* Search Bar */}
                    <div style={{ marginBottom: '24px' }}>
                        <div style={{
                            display: 'flex',
                            gap: '12px',
                            alignItems: 'center',
                            background: 'white',
                            padding: '12px 16px',
                            borderRadius: '16px',
                            boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
                        }}>
                            <span style={{ fontSize: '20px' }}>🔍</span>
                            <input
                                type="text"
                                placeholder="Tìm kiếm sản phẩm theo tên, mô tả, thương hiệu..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                style={{
                                    flex: 1,
                                    padding: '12px 0',
                                    border: 'none',
                                    fontSize: '15px',
                                    outline: 'none',
                                    background: 'transparent'
                                }}
                            />
                            {searchQuery && (
                                <button
                                    onClick={() => setSearchQuery('')}
                                    style={{
                                        background: '#eee',
                                        border: 'none',
                                        borderRadius: '50%',
                                        width: '28px',
                                        height: '28px',
                                        cursor: 'pointer',
                                        fontSize: '14px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center'
                                    }}
                                >
                                    ✕
                                </button>
                            )}
                        </div>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
                        <div>
                            <h2 style={{ fontSize: '28px', fontWeight: '700', marginBottom: '4px' }}>Marketplace</h2>
                            <p style={{ color: '#666', fontSize: '14px' }}>
                                {sortedProducts.length} sản phẩm
                                {selectedCategory !== 'all' && ` trong "${capitalizeWords(selectedCategory)}"`}
                                {searchQuery && ` khớp với "${searchQuery}"`}
                            </p>
                        </div>
                        <select
                            value={sortOption}
                            onChange={(e) => setSortOption(e.target.value)}
                            style={{
                                padding: '10px 16px',
                                borderRadius: '12px',
                                border: '1px solid #eee',
                                outline: 'none',
                                fontSize: '14px',
                                cursor: 'pointer',
                                backgroundColor: 'white'
                            }}
                        >
                            <option value="default">Sắp xếp: Mặc định</option>
                            <option value="price-low">Giá: Thấp đến Cao</option>
                            <option value="price-high">Giá: Cao đến Thấp</option>
                            <option value="name">Tên: A-Z</option>
                        </select>
                    </div>

                    {loading ? (
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            padding: '60px',
                            color: '#666'
                        }}>
                            <div style={{ textAlign: 'center' }}>
                                <div style={{ fontSize: '40px', marginBottom: '12px' }}>⏳</div>
                                <p>Đang tải sản phẩm...</p>
                            </div>
                        </div>
                    ) : sortedProducts.length === 0 ? (
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            padding: '60px',
                            color: '#666'
                        }}>
                            <div style={{ textAlign: 'center' }}>
                                <div style={{ fontSize: '50px', marginBottom: '16px' }}>📦</div>
                                <h3 style={{ marginBottom: '8px', color: '#333' }}>Không tìm thấy sản phẩm</h3>
                                <p>Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                                {(searchQuery || selectedCategory !== 'all') && (
                                    <button
                                        onClick={() => {
                                            setSearchQuery('');
                                            setSelectedCategory('all');
                                        }}
                                        style={{
                                            marginTop: '16px',
                                            padding: '10px 20px',
                                            background: '#c4161c',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '8px',
                                            cursor: 'pointer',
                                            fontWeight: '600'
                                        }}
                                    >
                                        Xóa bộ lọc
                                    </button>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="products">
                            {sortedProducts.map(product => (
                                <div key={product.productId} className="card">
                                    <Link to={`/product/${product.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                        <div className="card-top">
                                            <span className="tag">
                                                {capitalizeWords(product.categories?.[0]?.name) || 'Sản phẩm'}
                                            </span>
                                            {product.images && product.images.length > 0 ? (
                                                <img
                                                    src={product.images[0].imageUrl}
                                                    alt={product.name}
                                                    style={{
                                                        width: '100%',
                                                        height: '100%',
                                                        objectFit: 'cover',
                                                        position: 'absolute',
                                                        top: 0,
                                                        left: 0
                                                    }}
                                                />
                                            ) : (
                                                getAbbr(product.name)
                                            )}
                                        </div>
                                        <div className="card-body">
                                            <div className="title">{product.name}</div>
                                            <div className="price">{product.price ? product.price.toLocaleString() : '0'} đ</div>
                                            <button
                                                className="add-btn"
                                                disabled={isAdmin()}
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    if (isAdmin()) {
                                                        toast.info('Admin cannot purchase items');
                                                        return;
                                                    }
                                                    addToCart(product.productId, 1);
                                                    toast.success(`Đã thêm "${product.name}" vào giỏ hàng`);
                                                }}
                                                style={{
                                                    opacity: isAdmin() ? 0.5 : 1,
                                                    cursor: isAdmin() ? 'not-allowed' : 'pointer'
                                                }}
                                            >
                                                {isAdmin() ? '🔒 Admin View Only' : 'Add to cart'}
                                            </button>
                                        </div>
                                    </Link>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            </div>

            <Footer />
        </div>
    );
};

export default Shopping;