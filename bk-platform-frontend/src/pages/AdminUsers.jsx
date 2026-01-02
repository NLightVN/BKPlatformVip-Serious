import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { adminService } from '../services/adminService';
import AdminHeader from '../components/AdminHeader';
import { toast } from 'react-toastify';

const AdminUsers = () => {
    const { user, isAdmin } = useContext(AuthContext);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user || !isAdmin()) {
            navigate('/');
            return;
        }
        fetchUsers();
    }, [user, isAdmin, navigate]);

    const fetchUsers = async () => {
        try {
            const res = await adminService.getAllUsers();
            console.log('Admin users response:', res);
            if (res.code === 1000) {
                setUsers(res.result);
            } else {
                toast.error('Không thể tải danh sách users: ' + (res.message || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error fetching users:', error);
            toast.error('Lỗi khi tải users: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleBanToggle = async (userId, currentStatus) => {
        const shouldBan = currentStatus !== 'BANNED';
        try {
            const res = await adminService.banUser(userId, shouldBan);
            if (res.code === 1000) {
                toast.success(shouldBan ? 'Đã ban user' : 'Đã unban user');
                fetchUsers(); // Refresh list
            } else {
                toast.error('Lỗi: ' + (res.message || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error toggling ban:', error);
            toast.error('Lỗi khi ban/unban user');
        }
    };

    if (!user || !isAdmin()) return null;
    if (loading) return <div className="app-container"><AdminHeader /><main className="main-content">Loading...</main></div>;

    return (
        <div className="app-container">
            <AdminHeader />
            <main className="main-content" style={{ padding: '40px' }}>
                <h1>User Management</h1>

                <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px', background: 'white' }}>
                    <thead>
                        <tr style={{ background: '#f5f5f5', borderBottom: '2px solid #ddd' }}>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Username</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Email</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Roles</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Status</th>
                            <th style={{ padding: '12px', textAlign: 'left' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((u) => (
                            <tr key={u.userId} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: '12px' }}>{u.username}</td>
                                <td style={{ padding: '12px' }}>{u.email || '-'}</td>
                                <td style={{ padding: '12px' }}>{u.roleNames?.join(', ')}</td>
                                <td style={{ padding: '12px' }}>
                                    <span style={{
                                        padding: '4px 8px',
                                        borderRadius: '4px',
                                        background: u.status === 'BANNED' ? '#ff4444' : '#4caf50',
                                        color: 'white',
                                        fontSize: '12px'
                                    }}>
                                        {u.status || 'ACTIVE'}
                                    </span>
                                </td>
                                <td style={{ padding: '12px' }}>
                                    <button
                                        className="btn btn-sm"
                                        onClick={() => handleBanToggle(u.userId, u.status)}
                                        style={{
                                            background: u.status === 'BANNED' ? '#4caf50' : '#ff4444',
                                            color: 'white',
                                            border: 'none',
                                            padding: '6px 12px',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        {u.status === 'BANNED' ? 'Unban' : 'Ban'}
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </main>
        </div>
    );
};

export default AdminUsers;
