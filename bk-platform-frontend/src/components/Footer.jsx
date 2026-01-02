import React from 'react';

const Footer = () => {
    return (
        <footer>
            <div className="footer-container">
                <div>
                    <h3>BK Platform</h3>
                    <p>
                        BK Platform is a student-driven e-commerce platform built
                        by HUST students to connect buyers and sellers of study
                        materials and services.
                    </p>
                </div>

                <div>
                    <h3>Platform</h3>
                    <ul>
                        <li>About Us</li>
                        <li>Marketplace</li>
                        <li>Support</li>
                        <li>Terms & Privacy</li>
                    </ul>
                </div>

                <div>
                    <h3>Contact</h3>
                    <ul>
                        <li>📍 1 Dai Co Viet, Hai Ba Trung, Ha Noi</li>
                        <li>📞 0123 456 789</li>
                        <li>✉️ support@bkplatform.edu.vn</li>
                    </ul>
                </div>
            </div>

            <div className="footer-bottom">
                © 2026 BK Platform. All rights reserved.
            </div>
        </footer>
    );
};

export default Footer;