import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './layouts/MainLayout';
import Dashboard from './pages/Dashboard';
import Games from './pages/Games';
import Banners from './pages/Banners';
import Orders from './pages/Orders';
import Users from './pages/Users';
import Reviews from './pages/Reviews';
import Announcements from './pages/Announcements';
import './App.css';

// Protected route component
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    return <Navigate to="/login" replace />;  // React Router会自动添加basename
  }
  return children;
};

function App() {
  return (
    <BrowserRouter basename="/cms">
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />
        
        {/* Protected routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="games" element={<Games />} />
          <Route path="games/banners" element={<Banners />} />
          <Route path="users" element={<Users />} />
          <Route path="orders" element={<Orders />} />
          <Route path="reviews" element={<Reviews />} />
          <Route path="announcements" element={<Announcements />} />
          <Route path="settings" element={<div><h2>系统设置</h2></div>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
