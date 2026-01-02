import React from 'react';
import { Link } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';

const Home = () => {
  return (
    <>
      <Header />
      <div style={{ padding: '60px 20px', textAlign: 'center' }}>
        <h1 style={{ fontSize: '48px', marginBottom: '20px' }}>
          Chào mừng đến BK Platform
        </h1>
        <p style={{ fontSize: '20px', marginBottom: '40px', color: '#666' }}>
          Nền tảng thương mại điện tử cho sinh viên
        </p>
        <Link 
          to="/shopping" 
          style={{
            display: 'inline-block',
            padding: '15px 40px',
            backgroundColor: '#007bff',
            color: '#fff',
            textDecoration: 'none',
            borderRadius: '5px',
            fontSize: '18px'
          }}
        >
          Bắt đầu mua sắm
        </Link>
      </div>
      <Footer />
    </>
  );
};

export default Home;