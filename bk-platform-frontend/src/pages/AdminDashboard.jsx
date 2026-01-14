import React, { useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import AdminHeader from '../components/AdminHeader';

const AdminDashboard = () => {
    const { user, isAdmin } = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user || !isAdmin()) {
            navigate('/');
        }
    }, [user, isAdmin, navigate]);

    if (!user || !isAdmin()) return null;

    return (
        <div className="app-container">
            <AdminHeader />
            <main className="main-content" style={{ padding: '40px' }}>
                <h1>Admin Dashboard</h1>
                <p style={{ marginBottom: '40px', color: '#666' }}>
                    Welcome to the admin panel. Use the options below to manage the platform.
                </p>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '20px', marginTop: '30px' }}>
                    {/* Marketplace Card */}
                    <div
                        onClick={() => navigate('/shopping')}
                        style={{
                            background: 'white',
                            padding: '30px',
                            borderRadius: '12px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: 'pointer',
                            transition: 'transform 0.2s, box-shadow 0.2s'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-4px)';
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                        }}
                    >
                        <div style={{ fontSize: '40px', marginBottom: '15px' }}>🛒</div>
                        <h3 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>Marketplace</h3>
                        <p style={{ color: '#666', fontSize: '14px', margin: 0 }}>View products (read-only)</p>
                    </div>

                    {/* User Management Card */}
                    <div
                        onClick={() => navigate('/admin/users')}
                        style={{
                            background: 'white',
                            padding: '30px',
                            borderRadius: '12px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: 'pointer',
                            transition: 'transform 0.2s, box-shadow 0.2s'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-4px)';
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                        }}
                    >
                        <div style={{ fontSize: '40px', marginBottom: '15px' }}>👥</div>
                        <h3 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>User Management</h3>
                        <p style={{ color: '#666', fontSize: '14px', margin: 0 }}>View and manage users</p>
                    </div>

                    {/* Shop Management Card */}
                    <div
                        onClick={() => navigate('/admin/shops')}
                        style={{
                            background: 'white',
                            padding: '30px',
                            borderRadius: '12px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: 'pointer',
                            transition: 'transform 0.2s, box-shadow 0.2s'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-4px)';
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                        }}
                    >
                        <div style={{ fontSize: '40px', marginBottom: '15px' }}>🏪</div>
                        <h3 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>Shop Management</h3>
                        <p style={{ color: '#666', fontSize: '14px', margin: 0 }}>View and manage shops</p>
                    </div>
                </div>

                <style>{`
                    .admin-card {
                        background: white;
                        border: 2px solid #e0e0e0;
                        border-radius: 12px;
                        padding: 32px;
                        text-align: center;
                        cursor: pointer;
                        transition: all 0.3s;
                    }
                    
                    .admin-card:hover {
                        border-color: #007bff;
                        transform: translateY(-4px);
                        box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                    }

                    .admin-card h3 {
                        margin: 0 0 8px 0;
                        color: #333;
                    }

                    .admin-card p {
                        margin: 0;
                        color: #666;
                        font-size: 14px;
                    }
                `}</style>
            </main>
        </div>
    );
};

export default AdminDashboard;
