import request from '../utils/request';

export const getDashboardStats = () => {
  return request({
    url: '/dashboard/stats',
    method: 'get',
  });
};

export const getRevenueStats = (params) => {
  return request({
    url: '/dashboard/revenue',
    method: 'get',
    params,
  });
};

export const getUserGrowthStats = (params) => {
  return request({
    url: '/dashboard/user-growth',
    method: 'get',
    params,
  });
};

export const getTopGames = (limit = 10) => {
  return request({
    url: '/dashboard/top-games',
    method: 'get',
    params: { limit },
  });
};
