import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Footer from '../components/Footer';
import Header from '../components/Header';
import { locationService } from '../services/locationService';
import { authService } from '../services/authService';
import { toast } from 'react-toastify';

const Signup = () => {
const navigate = useNavigate();
const [formData, setFormData] = useState({
username: '', password: '', email: '', fullname: '',
phone: '', addressDetail: '', wardCode: ''
});


const [provinces, setProvinces] = useState([]);
const [districts, setDistricts] = useState([]);
const [wards, setWards] = useState([]);

const [selectedProvince, setSelectedProvince] = useState('');
const [selectedDistrict, setSelectedDistrict] = useState('');

useEffect(() => {
    locationService.getAllProvinces().then(res => {
        if(res.code === 1000) setProvinces(res.result);
    });
}, []);

const handleProvinceChange = (e) => {
    const provinceCode = e.target.value;
    setSelectedProvince(provinceCode);
    setDistricts([]); setWards([]);
    locationService.getDistrictsByProvince(provinceCode).then(res => {
        if(res.code === 1000) setDistricts(res.result);
    });
};

const handleDistrictChange = (e) => {
    const districtCode = e.target.value;
    setSelectedDistrict(districtCode);
    setWards([]);
    locationService.getWardsByDistrict(districtCode).then(res => {
        if(res.code === 1000) setWards(res.result);
    });
};

const handleSignup = async (e) => {
    e.preventDefault();
    
    // Validate HUST Email
    if (!formData.email.endsWith('@sis.hust.edu.vn')) {
        toast.warning("Vui lòng sử dụng email sinh viên (@sis.hust.edu.vn)");
        return;
    }

    const payload = {
        username: formData.username,
        password: formData.password,
        fullname: formData.fullname,
        email: formData.email,
        address: {
            name: formData.fullname,
            phone: formData.phone,
            addressDetail: formData.addressDetail,
            wardCode: formData.wardCode
        }
    };

    try {
        const res = await authService.register(payload);
        if(res.code === 1000) {
            toast.success("Đăng ký thành công! Hãy đăng nhập.");
            navigate('/login');
        } else {
            toast.error(res.message);
        }
    } catch (err) {
        toast.error(err.response?.data?.message || "Đăng ký thất bại");
    }
};

return (
    <div className="app-container">
        <Header />

        <main className="main-content" style={{display: 'flex', justifyContent: 'center', padding: '40px 0'}}>
            <div className="login-card" style={{height: 'auto', maxWidth: '700px'}}>
                <h2 style={{textAlign: 'center', marginBottom: '20px'}}>Đăng ký tài khoản</h2>
                <form onSubmit={handleSignup}>
                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap: '15px'}}>
                        <div className="form-group">
                            <label>Tên đăng nhập:</label>
                            <input className="login-input" value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})} required />
                        </div>
                        <div className="form-group">
                            <label>Mật khẩu (min 8 chars):</label>
                            <input className="login-input" type="password" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} required />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Họ và tên:</label>
                        <input className="login-input" value={formData.fullname} onChange={e => setFormData({...formData, fullname: e.target.value})} required />
                    </div>

                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap: '15px'}}>
                        <div className="form-group">
                            <label>Email Sinh Viên (HUST):</label>
                            <input className="login-input" type="email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} required />
                        </div>
                        <div className="form-group">
                            <label>Số điện thoại:</label>
                            <input className="login-input" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} required />
                        </div>
                    </div>

                    <label>Địa chỉ:</label>
                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'10px', marginBottom:'10px'}}>
                        <select className="login-input" onChange={handleProvinceChange} required>
                            <option value="">Tỉnh/Thành</option>
                            {provinces.map(p => <option key={p.code} value={p.code}>{p.fullName}</option>)}
                        </select>
                        <select className="login-input" onChange={handleDistrictChange} disabled={!selectedProvince} required>
                            <option value="">Quận/Huyện</option>
                            {districts.map(d => <option key={d.code} value={d.code}>{d.fullName}</option>)}
                        </select>
                        <select className="login-input" 
                            value={formData.wardCode} 
                            onChange={e => setFormData({...formData, wardCode: e.target.value})} 
                            disabled={!selectedDistrict} required>
                            <option value="">Phường/Xã</option>
                            {wards.map(w => <option key={w.code} value={w.code}>{w.fullName}</option>)}
                        </select>
                    </div>
                    
                    <div className="form-group">
                         <input className="login-input" placeholder="Số nhà, tên đường..." value={formData.addressDetail} onChange={e => setFormData({...formData, addressDetail: e.target.value})} required />
                    </div>

                    <button className="btn btn-primary" style={{width: '100%', marginTop: '10px'}} type="submit">Đăng Ký</button>
                    <p style={{ textAlign: 'center', marginTop: '15px', fontSize: '14px' }}>
                        Đã có tài khoản? <Link to="/login" style={{ color: '#c4161c', fontWeight: 'bold' }}>Đăng nhập ngay</Link>
                    </p>
                </form>
            </div>
        </main>
        <Footer />
    </div>
);

};

export default Signup;