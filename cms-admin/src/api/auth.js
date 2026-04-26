import axios from 'axios';
import { message } from 'antd';

// 创建axios实例
const request = axios.create({
  baseURL: '/api/cms',
  timeout: 30000,
});

// 请求拦截器
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

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    // 如果响应码不是200，显示错误
    if (res.code !== 200) {
      message.error(res.message || 'Error');
      
      // 401: 未授权
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

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  });
};

export const getAdminInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get',
  });
};
