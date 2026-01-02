import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminHeader = () => {
    const { user, logout, isAdmin } = useContext(AuthContext);
    const navigate = useNavigate();

    if (!isAdmin()) return null;

    return (
        <header className="header">
            <div className="logo" onClick={() => navigate('/admin')}>
                BK Platform - ADMIN
            </div>

            <div className="header-actions">
                {/* Marketplace (view only) */}
                <button className="btn btn-outline" onClick={() => navigate('/shopping')}>
                    🏪 Marketplace (View Only)
                </button>

                {/* Admin badge */}
                <button
                    onClick={() => navigate('/admin')}
                    style={{
                        padding: '8px 16px',
                        background: '#ff4444',
                        color: 'white',
                        borderRadius: '4px',
                        fontWeight: 'bold',
                        border: 'none',
                        cursor: 'pointer'
                    }}
                >
                    ADMIN
                </button>

                {/* User Info */}
                <span style={{ padding: '8px 16px' }}>
                    {user.username}
                </span>

                {/* Logout */}
                <button className="btn btn-outline" onClick={logout}>
                    Logout
                </button>
            </div>
        </header>
    );
};

export default AdminHeader;
