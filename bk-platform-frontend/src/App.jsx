import React from 'react';
import { Routes, Route } from 'react-router-dom';

import LandingPage from './pages/LandingPage';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Shopping from './pages/Shopping';
import Account from './pages/Account';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import SearchPage from './pages/SearchPage';
import ProductDetail from './pages/ProductDetail';
import ShopDetail from './pages/ShopDetail';
import CreateShop from './pages/CreateShop';
import SellerChannel from './pages/SellerChannel';
import CreateProduct from './pages/CreateProduct'; // <--- IMPORT MỚI
import UpdateProduct from './pages/UpdateProduct';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />

      <Route path="/shopping" element={<Shopping />} />
      <Route path="/account" element={<Account />} />
      <Route path="/cart" element={<Cart />} />
      <Route path="/checkout" element={<Checkout />} />
      <Route path="/search" element={<SearchPage />} />
      <Route path="/product/:id" element={<ProductDetail />} />
      <Route path="/shop/:id" element={<ShopDetail />} />

      {/* Route Tạo Shop */}
      <Route path="/create-shop" element={<CreateShop />} />

      {/* Route Kênh Người Bán */}
      <Route path="/seller-channel" element={<SellerChannel />} />

      {/* Route Tạo Sản Phẩm */}
      <Route path="/create-product" element={<CreateProduct />} />

      {/* Route Cập Nhật Sản Phẩm */}
      <Route path="/update-product/:id" element={<UpdateProduct />} />
    </Routes>
  );
}

export default App;