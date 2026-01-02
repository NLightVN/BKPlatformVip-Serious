import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { locationService } from '../services/locationService';
import { shopService } from '../services/shopService';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const CreateShop = () => {
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);

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
        locationService.getAllProvinces().then(res => {
            if (res.code === 1000) setProvinces(res.result);
        });
        
        // Tự động điền SĐT từ thông tin user nếu có
        if(user) {
            setFormData(prev => ({...prev, phone: user.address?.phone || ''}));
        }
    }, [user]);

    const handleProvinceChange = (e) => {
        const code = e.target.value;
        setFormData(prev => ({ ...prev, provinceCode: code, districtCode: '', wardCode: '' }));
        setDistricts([]); setWards([]);
        if (code) locationService.getDistrictsByProvince(code).then(res => res.code === 1000 && setDistricts(res.result));
    };

    const handleDistrictChange = (e) => {
        const code = e.target.value;
        setFormData(prev => ({ ...prev, districtCode: code, wardCode: '' }));
        setWards([]);
        if (code) locationService.getWardsByDistrict(code).then(res => res.code === 1000 && setWards(res.result));
    };

    const handleCreateShop = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                name: formData.name,
                address: {
                    name: formData.name, // Tên người liên hệ tại kho = Tên Shop
                    phone: formData.phone,
                    addressDetail: formData.addressDetail,
                    wardCode: formData.wardCode
                }
            };

            const res = await shopService.createShop(payload);
            if (res.code === 1000) {
                toast.success("Tạo Shop thành công!");
                navigate('/account'); // Quay về trang Account để thấy shop mới
            } else {
                toast.error(res.message);
            }
        } catch (err) {
            toast.error("Lỗi khi tạo shop");
        }
    };

    if (!user) return <div className="app-container"><Header /><main className="main-content">Vui lòng đăng nhập...</main></div>;

    return (
        <div className="app-container">
            <Header />
            <main className="main-content" style={{display: 'block', padding: '40px'}}>
                <div className="login-card" style={{height:'auto', maxWidth:'700px'}}>
                    <h2 style={{textAlign:'center', marginBottom:'20px'}}>Đăng ký mở Shop Bán Hàng</h2>
                    <form onSubmit={handleCreateShop}>
                        <div className="form-group">
                            <label>Tên Shop:</label>
                            <input className="login-input" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} required placeholder="VD: Shop Công Nghệ BK" />
                        </div>
                        <div className="form-group">
                            <label>Số điện thoại liên hệ:</label>
                            <input className="login-input" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} required />
                        </div>

                        <label style={{marginTop:'10px', display:'block', marginBottom:'5px', fontWeight:'bold', color:'#c4161c'}}>Địa chỉ kho hàng (Quan trọng để tính phí ship):</label>
                        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'10px'}}>
                            <select className="login-input" value={formData.provinceCode} onChange={handleProvinceChange} required>
                                <option value="">Tỉnh/Thành</option>
                                {provinces.map(p => <option key={p.code} value={p.code}>{p.fullName}</option>)}
                            </select>
                            <select className="login-input" value={formData.districtCode} onChange={handleDistrictChange} disabled={!formData.provinceCode} required>
                                <option value="">Quận/Huyện</option>
                                {districts.map(d => <option key={d.code} value={d.code}>{d.fullName}</option>)}
                            </select>
                            <select className="login-input" value={formData.wardCode} onChange={e => setFormData({...formData, wardCode: e.target.value})} disabled={!formData.districtCode} required>
                                <option value="">Phường/Xã</option>
                                {wards.map(w => <option key={w.code} value={w.code}>{w.fullName}</option>)}
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label>Địa chỉ chi tiết:</label>
                            <input className="login-input" value={formData.addressDetail} onChange={e => setFormData({...formData, addressDetail: e.target.value})} required placeholder="Số nhà, đường..."/>
                        </div>

                        <button className="login-btn" style={{width:'100%', marginTop:'15px'}} type="submit">Đăng ký Shop</button>
                    </form>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default CreateShop;