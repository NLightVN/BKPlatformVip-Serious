import React, { useContext, useState, useEffect } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { AuthContext } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { userService } from '../services/userService';
import { locationService } from '../services/locationService';
import { toast } from 'react-toastify';

const Account = () => {
    const { user, setUser } = useContext(AuthContext);
    const [orders, setOrders] = useState([]);
    const [activeTab, setActiveTab] = useState('info');
    const [expandedOrderId, setExpandedOrderId] = useState(null); // Để toggle xem chi tiết


    // Edit address states
    const [isEditingAddress, setIsEditingAddress] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        phone: '',
        addressDetail: '',
        provinceCode: '',
        districtCode: '',
        wardCode: ''
    });
    const [provinces, setProvinces] = useState([]);
    const [districts, setDistricts] = useState([]);
    const [wards, setWards] = useState([]);

    useEffect(() => {
        if (user && activeTab === 'history') {
            orderService.getHistory(user.userId).then(res => {
                if (res.code === 1000) setOrders(res.result);
            });
        }
    }, [user, activeTab]);

    // Load provinces when editing
    useEffect(() => {
        if (isEditingAddress) {
            locationService.getAllProvinces().then(res => {
                if (res.code === 1000) setProvinces(res.result);
            });

            // Pre-fill form with current address data
            if (user.address) {
                setFormData({
                    name: user.address.name || user.fullname || '',
                    phone: user.address.phone || '',
                    addressDetail: user.address.addressDetail || '',
                    provinceCode: user.address.province?.code || '',
                    districtCode: user.address.district?.code || '',
                    wardCode: user.address.ward?.code || ''
                });

                // Load districts and wards if address exists
                if (user.address.province?.code) {
                    locationService.getDistrictsByProvince(user.address.province.code)
                        .then(res => res.code === 1000 && setDistricts(res.result));
                }
                if (user.address.district?.code) {
                    locationService.getWardsByDistrict(user.address.district.code)
                        .then(res => res.code === 1000 && setWards(res.result));
                }
            }
        }
    }, [isEditingAddress, user]);

    const handleProvinceChange = (e) => {
        const code = e.target.value;
        setFormData(prev => ({ ...prev, provinceCode: code, districtCode: '', wardCode: '' }));
        setDistricts([]);
        setWards([]);
        if (code) {
            locationService.getDistrictsByProvince(code)
                .then(res => res.code === 1000 && setDistricts(res.result));
        }
    };

    const handleDistrictChange = (e) => {
        const code = e.target.value;
        setFormData(prev => ({ ...prev, districtCode: code, wardCode: '' }));
        setWards([]);
        if (code) {
            locationService.getWardsByDistrict(code)
                .then(res => res.code === 1000 && setWards(res.result));
        }
    };

    const handleSaveAddress = async () => {
        try {
            const payload = {
                fullname: user.fullname,
                email: user.email,
                address: {
                    name: formData.name,
                    phone: formData.phone,
                    addressDetail: formData.addressDetail,
                    wardCode: formData.wardCode
                }
            };

            const res = await userService.updateUser(user.userId, payload);
            if (res.code === 1000) {
                toast.success("Cập nhật địa chỉ thành công!");
                // Refresh user data
                const updatedUser = await userService.getMyInfo();
                if (updatedUser.code === 1000) {
                    setUser(updatedUser.result);
                }
                setIsEditingAddress(false);
            } else {
                toast.error(res.message || "Lỗi khi cập nhật địa chỉ");
            }
        } catch (err) {
            console.error(err);
            toast.error("Lỗi khi cập nhật địa chỉ");
        }
    };

    const handleCancelEdit = () => {
        setIsEditingAddress(false);
        setFormData({
            name: '',
            phone: '',
            addressDetail: '',
            provinceCode: '',
            districtCode: '',
            wardCode: ''
        });
    };

    const handleRequestCancel = async (orderId) => {
        if (!window.confirm("Bạn có chắc chắn muốn gửi yêu cầu hủy đơn hàng này?")) return;
        try {
            const res = await orderService.requestCancel(orderId);
            if (res.code === 1000) {
                toast.success("Đã gửi yêu cầu hủy thành công!");
                // Refresh list
                const updatedOrders = orders.map(o => o.orderId === orderId ? { ...o, cancellationRequested: true } : o);
                setOrders(updatedOrders);
            } else {
                toast.error(res.message || "Lỗi khi gửi yêu cầu");
            }
        } catch (err) {
            toast.error("Lỗi hệ thống");
        }
    };

    const getStatusBadge = (status, requestCancel) => {
        if (status === 'CANCELLED') return { text: 'Đã hủy', color: '#6c757d', bg: '#e2e3e5' };
        if (requestCancel) return { text: 'Đang yêu cầu hủy', color: '#ff9800', bg: '#fff3e0' };
        switch (status) {
            case 'PENDING': return { text: 'Đang xử lý', color: '#1565c0', bg: '#e3f2fd' };
            case 'PAID': return { text: 'Đã thanh toán', color: '#2e7d32', bg: '#e8f5e9' };
            case 'SHIPPED': return { text: 'Đang giao hàng', color: '#0277bd', bg: '#e1f5fe' };
            case 'DELIVERED': return { text: 'Giao thành công', color: '#1b5e20', bg: '#dcedc8' };
            default: return { text: status, color: '#333', bg: '#eee' };
        }
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ padding: '40px', alignItems: 'flex-start', justifyContent: 'center' }}>
                <div style={{ display: 'flex', gap: '30px', width: '100%', maxWidth: '1000px' }}>
                    {/* Sidebar */}
                    <div style={{ width: '250px', background: 'white', padding: '20px', borderRadius: '16px', height: 'fit-content' }}>
                        <div style={{ width: '80px', height: '80px', background: '#eee', borderRadius: '50%', margin: '0 auto 15px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '30px', color: '#c4161c', fontWeight: 'bold' }}>
                            {user.username.charAt(0).toUpperCase()}
                        </div>
                        <h3 style={{ textAlign: 'center', marginBottom: '20px' }}>{user.username}</h3>

                        <button className={`cat-btn ${activeTab === 'info' ? 'active-tab' : ''}`} onClick={() => setActiveTab('info')} style={{ marginBottom: '10px', fontWeight: activeTab === 'info' ? 'bold' : 'normal', color: activeTab === 'info' ? '#c4161c' : 'inherit' }}>
                            Thông tin tài khoản
                        </button>
                        <button className={`cat-btn ${activeTab === 'history' ? 'active-tab' : ''}`} onClick={() => setActiveTab('history')} style={{ fontWeight: activeTab === 'history' ? 'bold' : 'normal', color: activeTab === 'history' ? '#c4161c' : 'inherit' }}>
                            Lịch sử đơn hàng
                        </button>
                    </div>

                    {/* Content */}
                    <div style={{ flex: 1, background: 'white', padding: '30px', borderRadius: '16px', minHeight: '400px' }}>
                        {activeTab === 'info' && (
                            <div>
                                <h2 style={{ marginBottom: '20px' }}>Hồ sơ của tôi</h2>
                                <div style={{ display: 'grid', gap: '15px' }}>
                                    <div><strong>Họ tên:</strong> {user.fullname}</div>
                                    <div><strong>Email:</strong> {user.email}</div>

                                    {/* Address Section */}
                                    <div style={{ marginTop: '10px' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                                            <strong>Địa chỉ đặt hàng:</strong>
                                            {!isEditingAddress && (
                                                <button
                                                    className="btn"
                                                    onClick={() => setIsEditingAddress(true)}
                                                    style={{
                                                        background: '#c4161c',
                                                        color: 'white',
                                                        border: 'none',
                                                        padding: '5px 15px',
                                                        fontSize: '14px',
                                                        cursor: 'pointer'
                                                    }}
                                                >
                                                    ✏️ Chỉnh sửa
                                                </button>
                                            )}
                                        </div>

                                        {!isEditingAddress ? (
                                            // View mode
                                            <div style={{ padding: '15px', background: '#f5f5f5', borderRadius: '8px' }}>
                                                {user.address ? (
                                                    <>
                                                        <div>📍 {user.address.addressDetail}</div>
                                                        <div style={{ marginTop: '5px', fontSize: '14px', color: '#666' }}>
                                                            {user.address.ward?.fullName}, {user.address.district?.fullName}, {user.address.province?.fullName}
                                                        </div>
                                                        <div style={{ marginTop: '5px', fontSize: '14px', color: '#666' }}>
                                                            📞 {user.address.phone}
                                                        </div>
                                                    </>
                                                ) : (
                                                    <div style={{ color: '#999' }}>Chưa cập nhật địa chỉ</div>
                                                )}
                                            </div>
                                        ) : (
                                            // Edit mode
                                            <div style={{ padding: '20px', background: '#f9f9f9', borderRadius: '8px' }}>
                                                <div className="form-group" style={{ marginBottom: '15px' }}>
                                                    <label style={{ display: 'block', marginBottom: '5px' }}>Tên người nhận:</label>
                                                    <input
                                                        className="login-input"
                                                        value={formData.name}
                                                        onChange={e => setFormData({ ...formData, name: e.target.value })}
                                                        placeholder="VD: Nguyễn Văn A"
                                                    />
                                                </div>

                                                <div className="form-group" style={{ marginBottom: '15px' }}>
                                                    <label style={{ display: 'block', marginBottom: '5px' }}>Số điện thoại:</label>
                                                    <input
                                                        className="login-input"
                                                        value={formData.phone}
                                                        onChange={e => setFormData({ ...formData, phone: e.target.value })}
                                                        placeholder="VD: 0123456789"
                                                    />
                                                </div>

                                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                                                    <select className="login-input" value={formData.provinceCode} onChange={handleProvinceChange}>
                                                        <option value="">Tỉnh/Thành</option>
                                                        {provinces.map(p => <option key={p.code} value={p.code}>{p.fullName}</option>)}
                                                    </select>
                                                    <select className="login-input" value={formData.districtCode} onChange={handleDistrictChange} disabled={!formData.provinceCode}>
                                                        <option value="">Quận/Huyện</option>
                                                        {districts.map(d => <option key={d.code} value={d.code}>{d.fullName}</option>)}
                                                    </select>
                                                    <select className="login-input" value={formData.wardCode} onChange={e => setFormData({ ...formData, wardCode: e.target.value })} disabled={!formData.districtCode}>
                                                        <option value="">Phường/Xã</option>
                                                        {wards.map(w => <option key={w.code} value={w.code}>{w.fullName}</option>)}
                                                    </select>
                                                </div>

                                                <div className="form-group" style={{ marginBottom: '15px' }}>
                                                    <label style={{ display: 'block', marginBottom: '5px' }}>Địa chỉ chi tiết:</label>
                                                    <input
                                                        className="login-input"
                                                        value={formData.addressDetail}
                                                        onChange={e => setFormData({ ...formData, addressDetail: e.target.value })}
                                                        placeholder="Số nhà, đường..."
                                                    />
                                                </div>

                                                <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                                                    <button
                                                        className="login-btn"
                                                        onClick={handleSaveAddress}
                                                        style={{ flex: 1 }}
                                                    >
                                                        💾 Lưu
                                                    </button>
                                                    <button
                                                        className="btn"
                                                        onClick={handleCancelEdit}
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
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'history' && (
                            <div>
                                <h2 style={{ marginBottom: '20px' }}>Đơn hàng đã đặt</h2>
                                {orders.length > 0 ? (
                                    <table className="transaction-table">
                                        <thead>
                                            <tr>
                                                <th>Mã đơn</th>
                                                <th>Tổng tiền</th>
                                                <th>Trạng thái</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {orders.map(order => {
                                                const statusInfo = getStatusBadge(order.status, order.cancellationRequested);
                                                return (
                                                    <React.Fragment key={order.orderId}>
                                                        <tr onClick={() => setExpandedOrderId(expandedOrderId === order.orderId ? null : order.orderId)} style={{ cursor: 'pointer', borderBottom: '1px solid #eee' }}>
                                                            <td>#{order.orderId.substring(0, 8)}...</td>
                                                            <td style={{ color: '#c4161c', fontWeight: 'bold' }}>{order.totalAmount.toLocaleString()} đ</td>
                                                            <td>
                                                                <span style={{ padding: '4px 8px', background: statusInfo.bg, borderRadius: '10px', fontSize: '12px', color: statusInfo.color }}>
                                                                    {statusInfo.text}
                                                                </span>
                                                            </td>
                                                        </tr>
                                                        {expandedOrderId === order.orderId && (
                                                            <tr>
                                                                <td colSpan="3" style={{ padding: '15px', background: '#f9f9f9' }}>
                                                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                                                        <h4>Chi tiết đơn hàng</h4>
                                                                        <p style={{ fontSize: '13px', color: '#666' }}>Ngày đặt: {new Date(order.createdAt).toLocaleString('vi-VN')}</p>
                                                                        <div style={{ paddingLeft: '10px', borderLeft: '3px solid #ddd' }}>
                                                                            {order.items?.map((item, idx) => (
                                                                                <div key={idx} style={{ fontSize: '14px', marginBottom: '5px' }}>
                                                                                    • {item.productName} (x{item.quantity}) - {item.priceAtPurchase.toLocaleString()} đ
                                                                                </div>
                                                                            ))}
                                                                        </div>
                                                                        <p>Phí Ship: {order.shipment?.shippingFee?.toLocaleString() || 0} đ</p>

                                                                        {order.status === 'PENDING' && !order.cancellationRequested && (
                                                                            <button
                                                                                onClick={(e) => { e.stopPropagation(); handleRequestCancel(order.orderId); }}
                                                                                style={{ padding: '8px 15px', background: '#d9534f', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', alignSelf: 'flex-start', marginTop: '10px' }}
                                                                            >
                                                                                Hủy đơn hàng
                                                                            </button>
                                                                        )}
                                                                        {order.cancellationRequested && order.status !== 'CANCELLED' && (
                                                                            <p style={{ color: '#e65100', fontStyle: 'italic' }}>⏳ Đã gửi yêu cầu hủy - Chờ Shop duyệt</p>
                                                                        )}
                                                                    </div>
                                                                </td>
                                                            </tr>
                                                        )}
                                                    </React.Fragment>
                                                );
                                            })}
                                        </tbody>
                                    </table>
                                ) : (
                                    <p>Bạn chưa có đơn hàng nào.</p>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );

};

export default Account;