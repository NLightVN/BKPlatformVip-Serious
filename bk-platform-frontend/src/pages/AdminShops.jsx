import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { adminService } from '../services/adminService';
import AdminHeader from '../components/AdminHeader';
import { toast } from 'react-toastify';

const AdminShops = () => {
    const { user, isAdmin } = useContext(AuthContext);
    const [shops, setShops] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user || !isAdmin()) {
            navigate('/');
            return;
        }
        fetchShops();
    }, [user, isAdmin, navigate]);

    const fetchShops = async () => {
        try {
            // Use existing endpoint to get all shops
            const res = await fetch('http://localhost:8080/shops', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });
            const data = await res.json();
            console.log('Shops response:', data);
            if (data.code === 1000) {
                setShops(data.result);
            } else {
                toast.error('Không thể tải danh sách shop: ' + (data.message || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error fetching shops:', error);
            toast.error('Lỗi khi tải shops: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleBanToggle = async (shopId, currentStatus) => {
        const shouldBan = currentStatus !== 'BANNED';
        try {
            const res = await adminService.banShop(shopId, shouldBan);
            if (res.code === 1000) {
                toast.success(shouldBan ? 'Đã ban shop' : 'Đã gỡ ban shop');
                fetchShops(); // Refresh list
            } else {
                toast.error('Lỗi: ' + (res.message || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error toggling ban:', error);
            toast.error('Lỗi khi ban/unban shop');
        }
    };

    if (!user || !isAdmin()) return null;
    if (loading) return <div className="app-container"><AdminHeader /><main className="main-content">Đang tải...</main></div>;

    return (
        <div className="app-container">
            <AdminHeader />
            <main className="main-content" style={{ padding: '40px' }}>
                <h1>Shop Management</h1>

                <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px', background: 'white' }}>
                    <thead>
                        <tr style={{ background: '#f5f5f5', borderBottom: '2px solid #ddd' }}>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Shop Name</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Owner</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Address</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Status</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {shops.map((shop) => (
                            <tr key={shop.shopId} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: '12px' }}>
                                    <a
                                        href={`/shop/${shop.shopId}`}
                                        style={{ color: '#c4161c', textDecoration: 'none', fontWeight: 'bold' }}
                                        onClick={(e) => {
                                            e.preventDefault();
                                            navigate(`/shop/${shop.shopId}`);
                                        }}
                                    >
                                        {shop.name}
                                    </a>
                                </td>
                                <td style={{ padding: '12px' }}>{shop.ownerUsername || '-'}</td>
                                <td style={{ padding: '12px', fontSize: '13px', color: '#666' }}>
                                    {shop.address?.addressDetail || '-'}
                                </td>
                                <td style={{ padding: '12px' }}>
                                    <span style={{
                                        padding: '4px 8px',
                                        borderRadius: '4px',
                                        background: shop.status === 'BANNED' ? '#ff4444' : shop.status === 'DELETED' ? '#999' : '#4caf50',
                                        color: 'white',
                                        fontSize: '12px'
                                    }}>
                                        {shop.status || 'ACTIVE'}
                                    </span>
                                </td>
                                <td style={{ padding: '12px' }}>
                                    {shop.status !== 'DELETED' && (
                                        <button
                                            className="btn btn-sm"
                                            onClick={() => handleBanToggle(shop.shopId, shop.status)}
                                            style={{
                                                background: shop.status === 'BANNED' ? '#4caf50' : '#ff9800',
                                                color: 'white',
                                                border: 'none',
                                                padding: '6px 12px',
                                                borderRadius: '4px',
                                                cursor: 'pointer'
                                            }}
                                        >
                                            {shop.status === 'BANNED' ? 'Unban' : 'Ban'}
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </main>
        </div>
    );
};

export default AdminShops;
