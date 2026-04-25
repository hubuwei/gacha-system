import request from '../utils/request';

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
