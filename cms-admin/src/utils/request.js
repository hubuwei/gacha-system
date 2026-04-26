import axios from 'axios';
import { message } from 'antd';

const request = axios.create({
  baseURL: '/api/cms',
  timeout: 30000,
});

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    // If response code is not 200, show error
    if (res.code !== 200) {
      message.error(res.message || 'Error');
      
      // 401: Unauthorized
      if (res.code === 401) {
        localStorage.removeItem('admin_token');
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error(res.message || 'Error'));
    }
    
    return res;
  },
  (error) => {
    message.error(error.message || 'Network Error');
    return Promise.reject(error);
  }
);

export default request;
