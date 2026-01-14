import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom' // <--- THÊM DÒNG NÀY
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { CartProvider } from './context/CartContext.jsx'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* Bao bọc toàn bộ App bằng BrowserRouter */}
    <BrowserRouter> 
      <AuthProvider>
        <CartProvider>
          <App />
          <ToastContainer />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)