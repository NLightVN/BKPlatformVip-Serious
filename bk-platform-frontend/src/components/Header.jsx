import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { ShoppingCartContext } from '../context/CartContext';
import AdminHeader from './AdminHeader';

const Header = () => {
    const { user, logout, isAdmin } = useContext(AuthContext);
    const { cart } = useContext(ShoppingCartContext);
    const navigate = useNavigate();

    // If admin, show admin header
    if (user && isAdmin()) {
        return <AdminHeader />;
    }

    // Tính tổng số lượng item
    const cartCount = cart?.items?.reduce((acc, item) => acc + item.quantity, 0) || 0;

    const handleSellerChannelClick = () => {
        if (!user) return;
        // Navigate đến trang Kênh Người Bán để xem danh sách shop
        navigate('/seller-channel');
    };

    return (
        <header className="header">
            <div className="logo" onClick={() => navigate('/')}>
                BK Platform
            </div>

            <div className="header-actions">
                {/* Nút Mua sắm */}
                <button className="btn btn-outline" onClick={() => navigate('/shopping')}>
                    🏪 Marketplace
                </button>

                {/* Nút Kênh người bán */}
                {user && (
                    <button className="btn btn-outline" onClick={handleSellerChannelClick}>
                        Seller Channel
                    </button>
                )}

                {/* Nút Giỏ hàng */}
                <button className="cart-btn" onClick={() => navigate('/cart')}>
                    Cart ({cartCount})
                </button>

                {/* Account buttons */}
                {user ? (
                    <>
                        <button className="btn btn-primary" onClick={() => navigate('/account')}>
                            {user.username}
                        </button>
                        <button className="btn btn-outline" onClick={logout}>
                            Logout
                        </button>
                    </>
                ) : (
                    <button className="login-btn" onClick={() => navigate('/login')}>
                        Login
                    </button>
                )}
            </div>
        </header>
    );
};

export default Header;