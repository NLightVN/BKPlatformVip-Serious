import React, { useState, useContext } from 'react';
import Footer from './Footer';
import './index.css';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from './context/AuthContext';
import { toast } from 'react-toastify';

const Login = () => {
const [username, setUsername] = useState('');
const [password, setPassword] = useState('');
const { login } = useContext(AuthContext);
const navigate = useNavigate();

const handleLogin = async (e) => {
e.preventDefault();
try {
const success = await login(username, password);
if (success) {
toast.success("Đăng nhập thành công!");
navigate('/shopping');
} else {
toast.error("Sai tên đăng nhập hoặc mật khẩu");
}
} catch (err) {
toast.error("Lỗi kết nối server");
}
};

return (
<div className="app-container">
<header className="login-header">
<div className="login-logo">BK Platform</div>
<Link to="/signup"><button className="btn">Sign Up</button></Link>
</header>


<main className="login-main">
    <div className="login-card">
      <h2>Login</h2>
      <form onSubmit={handleLogin} style={{display:'flex', flexDirection:'column', width:'100%'}}>
          <label>Username:</label>
          <input 
            type="text" 
            className="login-input" 
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
          
          <label>Password:</label>
          <input 
            type="password" 
            className="login-input"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          
          <p className="login-hint">
            Don't you have an account? <Link to="/signup">Sign up</Link>
          </p>
          
          <button type="submit" className="login-btn">Login</button>
      </form>
    </div>
  </main>

  <Footer />
</div>

);
};

export default Login;