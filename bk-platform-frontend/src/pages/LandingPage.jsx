import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <div className="app-container">
            <Header />

            <main className="main-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '70vh' }}>
                <div className="welcome-card">
                    <div className="icon">BK</div>
                    <h1>Welcome to <span>BK Platform</span></h1>
                    <p>
                        A student-driven e-commerce platform built by HUST students
                        for buying, selling, and exchanging study materials, textbooks,
                        tools, clothing, and professional services such as tutoring,
                        design, and programming.
                    </p>
                    <div className="actions">
                        <button className="btn btn-primary" onClick={() => navigate('/signup')}>
                            Get Started
                        </button>
                        <button className="btn btn-outline" onClick={() => navigate('/shopping')}>
                            Explore Marketplace
                        </button>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
};

export default LandingPage;