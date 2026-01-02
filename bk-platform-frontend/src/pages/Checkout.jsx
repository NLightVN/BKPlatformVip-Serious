import React, { useContext, useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { ShoppingCartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { locationService } from '../services/locationService';
import { shopService } from '../services/shopService';
import axiosClient from '../api/axiosClient';
import { toast } from 'react-toastify';

const Checkout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { cart, fetchCart } = useContext(ShoppingCartContext);
    const { user } = useContext(AuthContext);

    const itemsToCheckout = location.state?.items || cart?.items || [];
    const isBuyNow = !!location.state?.items;

    // Tính tổng tiền hàng
    const subTotal = itemsToCheckout.reduce((acc, item) => acc + (item.price || 0) * item.quantity, 0);

    const [shippingFee, setShippingFee] = useState(0);
    const [loading, setLoading] = useState(false);
    const [isEditingAddress, setIsEditingAddress] = useState(false);

    // Form
    const [addressForm, setAddressForm] = useState({ fullname: '', phone: '', addressDetail: '', provinceCode: '', districtCode: '', wardCode: '' });
    const [provinces, setProvinces] = useState([]);
    const [districts, setDistricts] = useState([]);
    const [wards, setWards] = useState([]);

    useEffect(() => {
        if (user) {
            setAddressForm({
                fullname: user.fullname || '',
                phone: user.address?.phone || '',
                addressDetail: user.address?.addressDetail || '',
                provinceCode: user.address?.province?.code || '',
                districtCode: user.address?.district?.code || '',
                wardCode: user.address?.ward?.code || ''
            });
        }
    }, [user]);

    // Load địa chỉ khi edit
    useEffect(() => {
        if (isEditingAddress) {
            locationService.getAllProvinces().then(res => { if (res.code === 1000) setProvinces(res.result); });
        }
    }, [isEditingAddress]);

    // --- LOGIC TÍNH SHIP (FIXED) ---
    useEffect(() => {
        const calculateTotalShipping = async () => {
            // Lấy địa chỉ NHẬN (User)
            // Lưu ý: Cần optional chaining ?. kỹ càng
            const toWard = isEditingAddress ? addressForm.wardCode : user?.address?.ward?.code;
            const toDistrict = isEditingAddress ? addressForm.districtCode : user?.address?.district?.code;

            if (!toWard || !toDistrict || itemsToCheckout.length === 0) return;

            // 1. Nhóm sản phẩm theo ShopId
            const itemsByShop = {};
            itemsToCheckout.forEach(item => {
                // Nếu item không có shopId, gán là 'unknown'
                const sId = item.shopId || 'unknown';
                if (!itemsByShop[sId]) itemsByShop[sId] = [];
                itemsByShop[sId].push(item);
            });

            let totalFee = 0;
            const shopIds = Object.keys(itemsByShop);

            // 2. Tính ship cho từng shop
            await Promise.all(shopIds.map(async (shopId) => {
                let fromDistrict = "1488"; // Mặc định Hai Bà Trưng (Hà Nội)
                let fromWard = "1A0607";   // Mặc định Bách Khoa (Hà Nội)

                // Nếu shopId hợp lệ, lấy địa chỉ thực của shop
                if (shopId !== 'unknown') {
                    try {
                        const shopRes = await shopService.getShopById(shopId);
                        // Kiểm tra kỹ cấu trúc response
                        const shop = shopRes.result || shopRes;

                        console.log(`Checking Shop ${shop.name}:`, shop.address); // <--- DEBUG LOG

                        // Logic lấy code an toàn
                        if (shop.address) {
                            // Trường hợp 1: Backend trả về object (chuẩn)
                            if (shop.address.district && shop.address.district.code) {
                                fromDistrict = shop.address.district.code;
                                fromWard = shop.address.ward.code;
                            }
                            // Trường hợp 2: Backend trả về string code (nếu mapper cũ)
                            else if (shop.address.districtCode) {
                                fromDistrict = shop.address.districtCode;
                                fromWard = shop.address.wardCode;
                            }
                        }
                    } catch (e) {
                        console.warn("Không lấy được thông tin shop " + shopId);
                    }
                }

                // Tính tổng trọng lượng
                const shopItems = itemsByShop[shopId];
                const weight = shopItems.reduce((sum, i) => sum + (i.weight || 500) * i.quantity, 0);

                console.log(`Calculating Ship: ${fromDistrict} -> ${toDistrict} (${weight}g)`); // <--- DEBUG LOG

                try {
                    const payload = {
                        fromDistrictCode: String(fromDistrict), // Ép kiểu String cho chắc
                        fromWardCode: String(fromWard),
                        toDistrictCode: String(toDistrict),
                        toWardCode: String(toWard),
                        weightGram: weight > 0 ? weight : 1000
                    };
                    const res = await orderService.calculateShippingFee(payload);
                    if (res.code === 1000) {
                        totalFee += res.result.fee;
                    } else {
                        totalFee += 35000; // Fallback nếu API lỗi
                    }
                } catch (e) {
                    totalFee += 35000;
                }
            }));

            setShippingFee(totalFee);
        };

        calculateTotalShipping();
    }, [isEditingAddress, addressForm.wardCode, addressForm.districtCode, user, itemsToCheckout]);

    // Handlers
    const handleProvinceChange = (e) => {
        const code = e.target.value;
        setAddressForm(prev => ({ ...prev, provinceCode: code, districtCode: '', wardCode: '' }));
        setDistricts([]); setWards([]);
        if (code) locationService.getDistrictsByProvince(code).then(res => res.code === 1000 && setDistricts(res.result));
    };

    const handleDistrictChange = (e) => {
        const code = e.target.value;
        setAddressForm(prev => ({ ...prev, districtCode: code, wardCode: '' }));
        setWards([]);
        if (code) locationService.getWardsByDistrict(code).then(res => res.code === 1000 && setWards(res.result));
    };

    const handleSaveAddress = async () => {
        if (!addressForm.wardCode) return toast.warning("Vui lòng chọn địa chỉ");
        try {
            const payload = {
                fullname: addressForm.fullname,
                email: user.email,
                address: {
                    name: addressForm.fullname,
                    phone: addressForm.phone,
                    addressDetail: addressForm.addressDetail,
                    wardCode: addressForm.wardCode
                }
            };
            const res = await axiosClient.put(`/users/${user.userId}`, payload);
            if (res.code === 1000) {
                toast.success("Lưu địa chỉ thành công");
                setIsEditingAddress(false);
                window.location.reload();
            }
        } catch (err) { toast.error("Lỗi lưu địa chỉ"); }
    };

    const handleCheckout = async () => {
        if (!user?.address && !isEditingAddress) {
            toast.error("Vui lòng cập nhật địa chỉ!");
            setIsEditingAddress(true);
            return;
        }
        setLoading(true);
        try {
            let res;
            if (isBuyNow) {
                // Logic mua ngay
                const item = itemsToCheckout[0];
                res = await orderService.buyNow(item.productId, item.quantity);
            } else {
                // Logic checkout từ cart
                const productIds = itemsToCheckout.map(i => i.productId);
                res = await orderService.checkout(productIds);
            }

            if (res.code === 1000) {
                toast.success("Đặt hàng thành công!");
                if (!isBuyNow) await fetchCart(); // Refresh cart if checked out from cart
                navigate('/account');
            } else {
                toast.error(res.message || "Lỗi đặt hàng");
            }
        } catch (err) {
            // Show specific error message from backend
            const errorMessage = err.response?.data?.message || err.message || "Lỗi đặt hàng";
            toast.error(errorMessage);
        }
        finally { setLoading(false); }
    };

    const renderAddressString = () => {
        if (!user?.address) return <span style={{ color: 'red' }}>Chưa có địa chỉ</span>;
        const detail = user.address.addressDetail || '';
        const ward = user.address.ward?.fullName || '';
        const district = user.address.district?.fullName || '';
        const province = user.address.province?.fullName || '';
        return `${detail}, ${ward}, ${district}, ${province}`;
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{ display: 'block', padding: '40px' }}>
                <h2 style={{ marginBottom: '30px' }}>Thanh toán</h2>

                <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap', maxWidth: '1200px', margin: '0 auto' }}>
                    {/* CỘT TRÁI: ĐỊA CHỈ */}
                    <div style={{ flex: 1.5, minWidth: '350px' }}>
                        <div style={{ background: 'white', padding: '25px', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                                <h3>Địa chỉ nhận hàng</h3>
                                {!isEditingAddress && <button className="btn" style={{ fontSize: '12px', padding: '5px 15px' }} onClick={() => setIsEditingAddress(true)}>Thay đổi</button>}
                            </div>

                            {!isEditingAddress ? (
                                <div style={{ lineHeight: '1.8', fontSize: '15px' }}>
                                    <p><strong>Người nhận:</strong> {user.fullname}</p>
                                    <p><strong>SĐT:</strong> {user.address?.phone}</p>
                                    <p><strong>Địa chỉ:</strong> {renderAddressString()}</p>
                                </div>
                            ) : (
                                <div className="address-form">
                                    <div className="form-group"><label>Họ tên</label><input className="login-input" value={addressForm.fullname} onChange={e => setAddressForm({ ...addressForm, fullname: e.target.value })} /></div>
                                    <div className="form-group"><label>SĐT</label><input className="login-input" value={addressForm.phone} onChange={e => setAddressForm({ ...addressForm, phone: e.target.value })} /></div>

                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                                        <select className="login-input" value={addressForm.provinceCode} onChange={handleProvinceChange}><option value="">Tỉnh/Thành</option>{provinces.map(p => <option key={p.code} value={p.code}>{p.fullName}</option>)}</select>
                                        <select className="login-input" value={addressForm.districtCode} onChange={handleDistrictChange} disabled={!addressForm.provinceCode}><option value="">Quận/Huyện</option>{districts.map(d => <option key={d.code} value={d.code}>{d.fullName}</option>)}</select>
                                        <select className="login-input" value={addressForm.wardCode} onChange={e => setAddressForm({ ...addressForm, wardCode: e.target.value })} disabled={!addressForm.districtCode}><option value="">Phường/Xã</option>{wards.map(w => <option key={w.code} value={w.code}>{w.fullName}</option>)}</select>
                                    </div>
                                    <div className="form-group"><label>Chi tiết</label><input className="login-input" value={addressForm.addressDetail} onChange={e => setAddressForm({ ...addressForm, addressDetail: e.target.value })} /></div>
                                    <div style={{ display: 'flex', gap: '10px' }}>
                                        <button className="login-btn" onClick={handleSaveAddress}>Lưu địa chỉ</button>
                                        <button className="btn" onClick={() => setIsEditingAddress(false)}>Hủy</button>
                                    </div>
                                </div>
                            )}

                            <div style={{ marginTop: '30px', paddingTop: '20px', borderTop: '1px solid #eee' }}>
                                <h3>Phương thức thanh toán</h3>
                                <div style={{ marginTop: '15px', padding: '15px', border: '1px solid #c4161c', borderRadius: '8px', background: '#fff5f5', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                    <input type="radio" checked readOnly /> <strong>Thanh toán khi nhận hàng (COD)</strong>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* CỘT PHẢI: TỔNG QUAN */}
                    <div style={{ flex: 1, background: 'white', padding: '25px', borderRadius: '10px', height: 'fit-content', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                        <h3>Đơn hàng</h3>
                        <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                            {itemsToCheckout.map((item, idx) => (
                                <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '14px' }}>
                                    <span>{item.productName} <strong>x{item.quantity}</strong></span>
                                    <span>{Math.round(item.price * item.quantity).toLocaleString('vi-VN')} đ</span>
                                </div>
                            ))}
                        </div>
                        <hr style={{ margin: '20px 0', borderTop: '1px dashed #ccc' }} />
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}><span>Tạm tính:</span><span>{Math.round(subTotal).toLocaleString('vi-VN')} đ</span></div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}><span>Phí vận chuyển:</span><span>{Math.round(shippingFee).toLocaleString('vi-VN')} đ</span></div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '20px', fontWeight: 'bold', color: '#c4161c', marginTop: '15px', borderTop: '2px solid #eee', paddingTop: '15px' }}>
                            <span>Tổng cộng:</span><span>{Math.round(subTotal + shippingFee).toLocaleString('vi-VN')} đ</span>
                        </div>
                        <button className="login-btn" style={{ width: '100%', marginTop: '20px', padding: '12px', fontSize: '16px' }} onClick={handleCheckout} disabled={loading}>
                            {loading ? "Đang xử lý..." : "XÁC NHẬN ĐẶT HÀNG"}
                        </button>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};
export default Checkout;