import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const UpdateProduct = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);

    const [formData, setFormData] = useState({
        name: '',
        price: '',
        weight: '',
        brand: '',
        description: '',
        selectedCategories: [], // Array of selected category names (lowercase from DB)
        shopId: ''
    });

    const [image, setImage] = useState(null); // Single new image
    const [imagePreview, setImagePreview] = useState(null); // Single preview
    const [existingImage, setExistingImage] = useState(null); // Existing image from server
    const [loading, setLoading] = useState(true);
    const [categories, setCategories] = useState([]); // Categories from database

    // Helper to capitalize first letter of each word
    const capitalizeWords = (str) => {
        if (!str) return '';
        return str.split(' ').map(word =>
            word.charAt(0).toUpperCase() + word.slice(1)
        ).join(' ');
    };

    useEffect(() => {
        // Load categories from database
        categoryService.getAllCategories().then(res => {
            if (res.code === 1000 && res.result) {
                setCategories(res.result.map(cat => cat.name));
            }
        }).catch(err => {
            console.error('Could not load categories:', err);
        });

        // Load existing product data
        productService.getProductById(id).then(res => {
            if (res.code === 1000) {
                const product = res.result;

                setFormData({
                    name: product.name || '',
                    price: product.price || '',
                    weight: product.weight || '',
                    brand: product.brand || '',
                    description: product.description || '',
                    selectedCategories: product.categories?.map(c => c.name) || [],
                    shopId: product.shopId || ''
                });
                // Get first existing image only
                if (product.images && product.images.length > 0) {
                    setExistingImage(product.images[0]);
                }
            }
        }).finally(() => setLoading(false));
    }, [id]);

    const handleImageChange = (e) => {
        const file = e.target.files[0]; // Only first file
        if (file) {
            setImage(file);

            // Create preview
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const removeNewImage = () => {
        setImage(null);
        setImagePreview(null);
    };

    const handleCategoryChange = (categoryName) => {
        setFormData(prev => {
            const isSelected = prev.selectedCategories.includes(categoryName);
            if (isSelected) {
                return {
                    ...prev,
                    selectedCategories: prev.selectedCategories.filter(c => c !== categoryName)
                };
            } else {
                return {
                    ...prev,
                    selectedCategories: [...prev.selectedCategories, categoryName]
                };
            }
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const payload = {
                shopId: formData.shopId,
                name: formData.name,
                price: parseFloat(formData.price),
                weight: parseFloat(formData.weight),
                brand: formData.brand,
                description: formData.description,
                categoryNames: formData.selectedCategories
            };

            const res = await productService.updateProduct(id, payload);
            if (res.code === 1000) {
                // Upload new image if any
                if (image) {
                    try {
                        await productService.uploadProductImages(id, [image]);
                        toast.success("Cập nhật sản phẩm và tải ảnh thành công!");
                    } catch (imgErr) {
                        console.error(imgErr);
                        toast.warning("Cập nhật sản phẩm thành công nhưng lỗi khi tải ảnh");
                    }
                } else {
                    toast.success("Cập nhật sản phẩm thành công!");
                }
                navigate(`/product/${id}`);
            } else {
                toast.error(res.message || "Lỗi khi cập nhật sản phẩm");
            }
        } catch (err) {
            console.error(err);
            toast.error("Lỗi khi cập nhật sản phẩm");
        }
    };

    if (loading) return <div className="app-container"><Header /><main className="main-content">Đang tải...</main></div>;

    if (!user) {
        return (
            <div className="app-container">
                <Header />
                <main className="main-content">Vui lòng đăng nhập...</main>
            </div>
        );
    }

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                <div className="login-card" style={{ height: 'auto', maxWidth: '700px' }}>
                    <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Cập Nhật Sản Phẩm</h2>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Tên sản phẩm: <span style={{ color: 'red' }}>*</span></label>
                            <input
                                className="login-input"
                                value={formData.name}
                                onChange={e => setFormData({ ...formData, name: e.target.value })}
                                required
                                placeholder="VD: iPhone 15 Pro Max"
                            />
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                            <div className="form-group">
                                <label>Giá (VNĐ): <span style={{ color: 'red' }}>*</span></label>
                                <input
                                    className="login-input"
                                    type="number"
                                    value={formData.price}
                                    onChange={e => setFormData({ ...formData, price: e.target.value })}
                                    required
                                    min="0"
                                    step="1000"
                                    placeholder="VD: 25000000"
                                />
                            </div>

                            <div className="form-group">
                                <label>Khối lượng (kg): <span style={{ color: 'red' }}>*</span></label>
                                <input
                                    className="login-input"
                                    type="number"
                                    value={formData.weight}
                                    onChange={e => setFormData({ ...formData, weight: e.target.value })}
                                    required
                                    min="0"
                                    step="0.01"
                                    placeholder="VD: 0.5"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Thương hiệu:</label>
                            <input
                                className="login-input"
                                value={formData.brand}
                                onChange={e => setFormData({ ...formData, brand: e.target.value })}
                                placeholder="VD: Apple"
                            />
                        </div>

                        <div className="form-group">
                            <label>Danh mục:</label>
                            {categories.length === 0 ? (
                                <p style={{ color: '#666', fontSize: '14px' }}>Đang tải danh mục...</p>
                            ) : (
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(3, 1fr)',
                                    gap: '8px',
                                    maxHeight: '200px',
                                    overflowY: 'auto',
                                    padding: '12px',
                                    border: '1px solid #ddd',
                                    borderRadius: '8px',
                                    background: '#fafafa'
                                }}>
                                    {categories.map(cat => {
                                        const isSelected = formData.selectedCategories.includes(cat);
                                        return (
                                            <label
                                                key={cat}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '6px',
                                                    padding: '8px 10px',
                                                    borderRadius: '6px',
                                                    cursor: 'pointer',
                                                    background: isSelected ? '#fde1e1' : 'white',
                                                    border: isSelected ? '2px solid #c4161c' : '1px solid #ddd',
                                                    fontSize: '13px',
                                                    transition: 'all 0.2s'
                                                }}
                                            >
                                                <input
                                                    type="checkbox"
                                                    checked={isSelected}
                                                    onChange={() => handleCategoryChange(cat)}
                                                    style={{ accentColor: '#c4161c' }}
                                                />
                                                {capitalizeWords(cat)}
                                            </label>
                                        );
                                    })}
                                </div>
                            )}
                            {formData.selectedCategories.length > 0 && (
                                <small style={{ color: '#666', fontSize: '12px', marginTop: '8px', display: 'block' }}>
                                    Đã chọn: {formData.selectedCategories.map(c => capitalizeWords(c)).join(', ')}
                                </small>
                            )}
                        </div>

                        <div className="form-group">
                            <label>Mô tả:</label>
                            <textarea
                                className="login-input"
                                value={formData.description}
                                onChange={e => setFormData({ ...formData, description: e.target.value })}
                                rows="4"
                                placeholder="Mô tả chi tiết về sản phẩm..."
                                style={{ resize: 'vertical', fontFamily: 'inherit' }}
                            />
                        </div>

                        {/* Existing Image */}
                        {existingImage && !imagePreview && (
                            <div className="form-group">
                                <label>Hình ảnh hiện tại:</label>
                                <div style={{ marginTop: '10px' }}>
                                    <div style={{ width: '120px', height: '120px' }}>
                                        <img
                                            src={existingImage.imageUrl}
                                            alt="Existing"
                                            style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px' }}
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* New Image */}
                        <div className="form-group">
                            <label>{existingImage ? 'Thay đổi hình ảnh (1 ảnh):' : 'Hình ảnh sản phẩm (1 ảnh):'}</label>
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handleImageChange}
                                style={{ display: 'none' }}
                                id="image-upload"
                            />
                            <label
                                htmlFor="image-upload"
                                style={{
                                    display: 'inline-block',
                                    padding: '10px 20px',
                                    background: '#f0f0f0',
                                    border: '2px dashed #ccc',
                                    borderRadius: '8px',
                                    cursor: 'pointer',
                                    textAlign: 'center',
                                    width: '100%'
                                }}
                            >
                                📷 {existingImage ? 'Chọn ảnh mới để thay thế' : 'Chọn hình ảnh'}
                            </label>
                            {imagePreview && (
                                <div style={{ marginTop: '10px' }}>
                                    <div style={{ position: 'relative', width: '120px', height: '120px', display: 'inline-block' }}>
                                        <img
                                            src={imagePreview}
                                            alt="Preview"
                                            style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px' }}
                                        />
                                        <button
                                            type="button"
                                            onClick={removeNewImage}
                                            style={{
                                                position: 'absolute',
                                                top: '-8px',
                                                right: '-8px',
                                                background: '#ff4444',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '50%',
                                                width: '28px',
                                                height: '28px',
                                                cursor: 'pointer',
                                                fontSize: '16px',
                                                lineHeight: '16px',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center'
                                            }}
                                        >
                                            ×
                                        </button>
                                    </div>
                                    <small style={{ color: '#666', display: 'block', marginTop: '4px' }}>
                                        Ảnh mới sẽ thay thế ảnh cũ
                                    </small>
                                </div>
                            )}
                        </div>

                        <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                            <button
                                className="login-btn"
                                type="submit"
                                style={{ flex: 1 }}
                            >
                                ✅ Cập Nhật Sản Phẩm
                            </button>
                            <button
                                className="btn"
                                type="button"
                                onClick={() => navigate(`/product/${id}`)}
                                style={{
                                    flex: 1,
                                    background: '#999',
                                    color: 'white',
                                    border: 'none',
                                    cursor: 'pointer'
                                }}
                            >
                                ❌ Hủy
                            </button>
                        </div>
                    </form>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default UpdateProduct;
