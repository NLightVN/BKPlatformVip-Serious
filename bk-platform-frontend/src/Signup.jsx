import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Footer from './Footer';
import { locationService } from './services/locationService';
import { authService } from './services/authService';
import { toast } from 'react-toastify';

const Signup = () => {
const navigate = useNavigate();
const [formData, setFormData] = useState({
username: '', password: '', email: '', fullname: '',
phone: '', addressDetail: '', wardCode: ''
});


// States for Address Dropdowns
const [provinces, setProvinces] = useState([]);
const [districts, setDistricts] = useState([]);
const [wards, setWards] = useState([]);

const [selectedProvince, setSelectedProvince] = useState('');
const [selectedDistrict, setSelectedDistrict] = useState('');

useEffect(() => {
    // Load Provinces
    locationService.getAllProvinces().then(res => {
        if(res.code === 1000) setProvinces(res.result);
    });
}, []);

const handleProvinceChange = (e) => {
    const provinceCode = e.target.value;
    setSelectedProvince(provinceCode);
    setDistricts([]);
    setWards([]);
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
    
    // Construct the request body matching Backend DTO
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
            toast.error(res.message || "Đăng ký thất bại");
        }
    } catch (err) {
        toast.error(err.response?.data?.message || "Lỗi hệ thống");
    }
};

return (
    <div className="app-container">
        <header className="login-header">
            <div className="login-logo">BK Platform</div>
            <Link to="/login"><button className="btn">Login</button></Link>
        </header>

        <main className="login-main" style={{padding: '40px 0'}}>
            <div className="login-card" style={{height: 'auto', maxWidth: '600px'}}>
                <h2>Create account</h2>
                <form onSubmit={handleSignup} className="checkout-form">
                    <div className="form-group">
                        <input className="login-input" placeholder="Username" value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})} required />
                    </div>
                    <div className="form-group">
                        <input className="login-input" type="password" placeholder="Password" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} required />
                    </div>
                    <div className="form-group">
                        <input className="login-input" type="email" placeholder="Email (@sis.hust.edu.vn)" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} required />
                    </div>
                    <div className="form-group">
                        <input className="login-input" placeholder="Full Name" value={formData.fullname} onChange={e => setFormData({...formData, fullname: e.target.value})} required />
                    </div>
                    <div className="form-group">
                        <input className="login-input" placeholder="Phone" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} required />
                    </div>

                    {/* Address Selection */}
                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'10px', width:'100%', marginBottom:'15px'}}>
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
                         <input className="login-input" placeholder="Số nhà, đường..." value={formData.addressDetail} onChange={e => setFormData({...formData, addressDetail: e.target.value})} required />
                    </div>

                    <button className="login-btn" type="submit">Create</button>
                </form>
            </div>
        </main>
        <Footer />
    </div>
);

};

export default Signup;