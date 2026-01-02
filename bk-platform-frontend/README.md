# BK Platform Frontend

## Giới thiệu
Frontend cho hệ thống BK Platform - nền tảng thương mại điện tử.

## Công nghệ sử dụng
- React 18
- React Router DOM
- Axios
- React Toastify
- Vite

## Cài đặt

```bash
# Cài đặt dependencies
npm install

# Chạy development server
npm run dev

# Build production
npm run build
```

## Cấu trúc thư mục
```
src/
├── api/              # Cấu hình API
├── context/          # React Context (Auth, Cart)
├── services/         # API Services
└── components/       # React Components
```

## Lưu ý
- Backend API mặc định: http://localhost:8080
- Cần chạy backend Spring Boot trước khi sử dụng
